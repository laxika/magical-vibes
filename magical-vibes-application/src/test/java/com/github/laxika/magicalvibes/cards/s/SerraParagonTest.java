package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.d.DestroyEvil;
import com.github.laxika.magicalvibes.cards.e.EchoingDeeps;
import com.github.laxika.magicalvibes.cards.e.EssenceScatter;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerraParagon.class, Forest.class, WalkingCorpse.class, Shock.class, StoneRain.class,
        ShivanDevastator.class, DestroyEvil.class, EchoingDeeps.class, ActOfTreason.class,
        SpectralSailor.class, EssenceScatter.class})
class SerraParagonTest extends BaseCardTest {

    @Test
    @DisplayName("A permanent cast from the graveyard gains the exile and life trigger")
    void permanentCastFromGraveyardGainsTrigger() {
        harness.addToBattlefield(player1, new SerraParagon());
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(corpse));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent corpsePermanent = findPermanent(player1, "Walking Corpse");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, corpsePermanent.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Walking Corpse");
        harness.assertNotInGraveyard(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("Playing a land from the graveyard uses Serra Paragon's once-per-turn permission")
    void playingLandUsesOncePerTurnPermission() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(new Forest(), new WalkingCorpse()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.playGraveyardLand(player1, 0));

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A land played from the graveyard gains the exile and life trigger")
    void landPlayedFromGraveyardGainsTrigger() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.playGraveyardLand(player1, 0));

        Permanent forest = findPermanent(player1, "Forest");
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.clearPriorityPassed();
        harness.castSorcery(player1, 0, forest.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Forest");
    }

    @Test
    @DisplayName("Casting a permanent from the graveyard uses the same permission as playing a land")
    void castingPermanentUsesOncePerTurnPermission() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new Forest()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castFromGraveyard(player1, 0);
            harness.passBothPriorities();
        });
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsPermanentWithManaValueAboveThree() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(new SerraParagon()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNonpermanentSpell() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stillRequiresPaymentOfManaCost() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Walking Corpse");
    }

    @Test
    void cannotUsePermissionDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(new Forest(), new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void permissionDoesNotOverrideNormalTiming() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(new Forest(), new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void permissionDoesNotGrantAdditionalLandPlay() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setHand(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void twoParagonsAllowTwoSpellsWithOneGrantedTriggerEach() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Walking Corpse")).isEqualTo(2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Walking Corpse").getId());
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        assertThat(countPermanents(player1, "Walking Corpse")).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void grantedTriggerRemainsAfterParagonLeaves(boolean beforeSpellResolves) {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromGraveyard(player1, 0);
        if (!beforeSpellResolves) {
            harness.passBothPriorities();
        }

        harness.setHand(player1, List.of(new DestroyEvil(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castModalInstant(player1, 0, 0, List.of(findPermanent(player1, "Serra Paragon").getId()));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Serra Paragon");
        if (beforeSpellResolves) {
            harness.passBothPriorities();
        }
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Walking Corpse").getId());
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertNotInGraveyard(player1, "Walking Corpse");
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(card -> card.getName())
                .contains("Walking Corpse");
    }

    @Test
    void xSpellAtManaValueThreeIsAllowed() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(new ShivanDevastator()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.ensurePriority(player1);
        gs.playFlashbackSpell(gd, player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Shivan Devastator").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    void xSpellAboveManaValueThreeIsRejected() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(new ShivanDevastator()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, 3, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void landWithCopyChoiceRetainsGrantedTrigger(boolean copy) {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(new EchoingDeeps()));
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.playGraveyardLand(player1, 0));
        harness.handleMayAbilityChosen(player1, copy);
        if (copy) {
            harness.handleGraveyardCardChosen(player1, 0);
        }

        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(player1, 0, findPermanent(player1, copy ? "Forest" : "Echoing Deeps").getId());
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertNotInGraveyard(player1, "Echoing Deeps");
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(card -> card.getName())
                .contains("Echoing Deeps");
    }

    @Test
    void newControllerReceivesLifeFromGrantedTrigger() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        Permanent corpse = findPermanent(player1, "Walking Corpse");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ActOfTreason(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player2, 0, corpse.getId());
        harness.assertOnBattlefield(player2, "Walking Corpse");
        harness.castAndResolveInstant(player2, 0, corpse.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(card -> card.getName())
                .contains("Walking Corpse");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void oldTriggerCannotFollowRecastCard(boolean counterRecast) {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.addToBattlefield(player1, new SerraParagon());
        SpectralSailor sailor = new SpectralSailor();
        harness.setGraveyard(player1, List.of(sailor));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Spectral Sailor").getId());
        assertThat(gd.stack).hasSize(1);
        harness.castFromGraveyard(player1, 0);
        if (counterRecast) {
            harness.setHand(player2, List.of(new EssenceScatter()));
            harness.addMana(player2, ManaColor.BLUE, 2);
            harness.castAndResolveInstant(player2, 0, sailor.getId());
            harness.assertInGraveyard(player1, "Spectral Sailor");
        } else {
            harness.passBothPriorities();
            harness.assertOnBattlefield(player1, "Spectral Sailor");
        }
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        if (counterRecast) {
            harness.assertInGraveyard(player1, "Spectral Sailor");
        } else {
            harness.assertOnBattlefield(player1, "Spectral Sailor");
        }
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(card -> card.getName())
                .doesNotContain("Spectral Sailor");
    }

    @Test
    void permissionAndLandAllowanceResetOnNextTurn() {
        harness.addToBattlefield(player1, new SerraParagon());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.playGraveyardLand(player1, 0));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> harness.playGraveyardLand(player1, 0));

        assertThat(countPermanents(player1, "Forest")).isEqualTo(2);
    }
}
