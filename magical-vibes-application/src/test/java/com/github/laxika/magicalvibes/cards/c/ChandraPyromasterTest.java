package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AltarsReap;
import com.github.laxika.magicalvibes.cards.j.JaceMemoryAdept;
import com.github.laxika.magicalvibes.cards.p.PayNoHeed;
import com.github.laxika.magicalvibes.cards.s.ScavengingOoze;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Silence;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandraPyromaster.class, Forest.class, ScavengingOoze.class, JaceMemoryAdept.class,
        Shock.class, PayNoHeed.class, AltarsReap.class, Silence.class})
class ChandraPyromasterTest extends BaseCardTest {

    @Test
    @DisplayName("+1 pings the target player and a creature they control, which then can't block")
    void plusOneHitsPlayerAndTheirCreature() {
        Permanent chandra = addReadyChandra(player1, 4);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new ScavengingOoze());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), bear.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(bear.getMarkedDamage()).isEqualTo(1);
        assertThat(bear.isCantBlockThisTurn()).isTrue();
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 may choose the player alone, leaving every creature able to block")
    void plusOneCreatureTargetIsOptional() {
        addReadyChandra(player1, 4);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new ScavengingOoze());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(bear.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("+1 can target a planeswalker and a creature its controller controls")
    void plusOneHitsPlaneswalkerAndItsControllersCreature() {
        addReadyChandra(player1, 4);
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceMemoryAdept());
        jace.setCounterCount(CounterType.LOYALTY, 4);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new ScavengingOoze());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(jace.getId(), bear.getId()));
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(bear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("+1 rejects a creature the targeted player does not control")
    void plusOneRejectsCreatureOfAnotherController() {
        addReadyChandra(player1, 4);
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new ScavengingOoze());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player2.getId(), ownBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("0 exiles the top card and grants permission to play it this turn")
    void zeroExilesTopCardWithPlayPermission() {
        Permanent chandra = addReadyChandra(player1, 4);
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top, new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
        // Chandra's 0 grants a normal-cost play, not a free one.
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(top.getId());
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-7 exiles ten cards and asks which exiled instant or sorcery to copy")
    void ultimateExilesTenAndPromptsForCopyChoice() {
        addReadyChandra(player1, 7);
        Shock shock = new Shock();
        List<Card> library = new ArrayList<>();
        library.add(shock);
        for (int i = 0; i < 11; i++) {
            library.add(new Forest());
        }
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(10);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledSpellCopyChoice.class);
    }

    @Test
    @DisplayName("-7 casts three free copies of the chosen instant or sorcery")
    void ultimateCastsThreeCopies() {
        addReadyChandra(player1, 7);
        Shock shock = new Shock();
        List<Card> library = new ArrayList<>();
        library.add(shock);
        for (int i = 0; i < 11; i++) {
            library.add(new Forest());
        }
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack.stream().filter(e -> e.getCard().getName().equals("Shock")).count())
                .isEqualTo(3);
        assertThat(gd.stack.stream().filter(e -> e.getCard().getName().equals("Shock")))
                .allMatch(e -> e.isCopy());
    }

    @Test
    @DisplayName("-7 does nothing further when no instant or sorcery is exiled")
    void ultimateWithoutInstantOrSorcery() {
        addReadyChandra(player1, 7);
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            library.add(new Forest());
        }
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(10);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void preventedSourceDamageStillStopsCreatureBlocking() {
        Permanent chandra = addReadyChandra(player1, 4);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScavengingOoze());
        harness.setHand(player2, List.of(new PayNoHeed()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), creature.getId()));
        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, chandra.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void creatureStillCannotBlockWhenTargetPlaneswalkerLeaves() {
        addReadyChandra(player1, 4);
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceMemoryAdept());
        jace.setCounterCount(CounterType.LOYALTY, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScavengingOoze());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(jace.getId(), creature.getId()));
        harness.castInstant(player1, 0, jace.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Jace, Memory Adept");
        harness.passBothPriorities();

        assertThat(creature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void zeroAllowsLandPlayButDoesNotGrantExtraLandPlay() {
        addReadyChandra(player1, 4);
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land, new Forest()));
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.playLand(player1, 0)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void zeroRequiresPayingTheExiledSpellsManaCost() {
        addReadyChandra(player1, 4);
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock, new Forest()));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void zeroPermissionExpiresAtEndOfTurn() {
        addReadyChandra(player1, 4);
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock, new Forest(), new Forest()));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    void zeroWithEmptyLibraryDoesNotLoseTheGame() {
        addReadyChandra(player1, 4);
        harness.setLibrary(player1, List.of());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertOnBattlefield(player1, "Chandra, Pyromaster");
    }

    @Test
    void ultimateRejectsSpellExiledBeforeThisActivation() {
        addReadyChandra(player1, 7);
        Shock oldShock = new Shock();
        Shock newShock = new Shock();
        harness.setExile(player1, List.of(oldShock));
        harness.setLibrary(player1, List.of(newShock, new Forest()));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(oldShock.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(oldShock, newShock);
    }

    @Test
    void ultimateCopiesResolveAndLeaveTheOriginalExiled() {
        addReadyChandra(player1, 7);
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock, new Forest()));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        for (int i = 0; i < 3; i++) {
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
                harness.handleMayAbilityChosen(player1, true);
            }
            harness.handlePermanentChosen(player1, player2.getId());
        }
        for (int i = 0; i < 3; i++) {
            harness.passBothPriorities();
        }

        harness.assertLife(player2, 14);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ultimateOffersIndependentChoicesToDeclineEachCopy() {
        addReadyChandra(player1, 7);
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock, new Forest()));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));

        for (int i = 0; i < 3; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
        harness.assertLife(player2, 20);
    }

    @Test
    void ultimateAllowsCopiesWithPayableAdditionalCosts() {
        addReadyChandra(player1, 7);
        harness.addToBattlefield(player1, new ScavengingOoze());
        AltarsReap reap = new AltarsReap();
        harness.setLibrary(player1, List.of(reap, new Forest()));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(reap.getId()));

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(reap);
    }

    @Test
    void ultimateCanCastOneCopyAndDeclineTheOtherTwo() {
        addReadyChandra(player1, 7);
        Shock shock = new Shock();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(shock, forest));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock, forest);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ultimateCannotCastCopiesThroughSilence() {
        addReadyChandra(player1, 7);
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock, new Forest()));
        harness.setHand(player2, List.of(new Silence()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));

        for (int i = 0; i < 3; i++) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChandraPyromaster());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
