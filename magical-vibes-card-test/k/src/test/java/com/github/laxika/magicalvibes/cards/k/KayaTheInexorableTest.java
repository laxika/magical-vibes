package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IonasJudgment;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.cards.t.TyvarKell;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KayaTheInexorable.class, GrizzlyBears.class, IonasJudgment.class, Shock.class,
        TyvarKell.class, SnowCoveredPlains.class})
class KayaTheInexorableTest extends BaseCardTest {

    @Test
    @DisplayName("+1 returns a ghostform creature to its owner's hand when it dies and creates a Spirit")
    void plusOneReturnsCreatureFromGraveyard() {
        Permanent kaya = addReadyKaya(4);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(kaya), 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.GHOSTFORM)).isEqualTo(1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player2, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("+1 returns a ghostform creature to its owner's hand when it is exiled and creates a Spirit")
    void plusOneReturnsCreatureFromExile() {
        Permanent kaya = addReadyKaya(4);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(kaya), 0, null, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new IonasJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("-7 emblem offers a legendary spell from hand and casts it without paying its mana cost")
    void ultimateCastsLegendarySpellFromHand() {
        Permanent kaya = addReadyKaya(7);
        harness.activateAbility(player1, battlefieldIndex(kaya), 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new TyvarKell()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Tyvar Kell");
    }

    @Test
    void plusOneCanResolveWithoutATarget() {
        Permanent kaya = addReadyKaya(5);

        harness.activateAbility(player1, battlefieldIndex(kaya), 0, null, null);
        resolveAllTriggers();

        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    void minusThreeExilesOpposingPlaneswalker() {
        Permanent kaya = addReadyKaya(5);
        Permanent tyvar = harness.addToBattlefieldAndReturn(player2, new TyvarKell());
        tyvar.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, battlefieldIndex(kaya), 1, null, tyvar.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Tyvar Kell");
        assertThat(gd.findExiledCard(tyvar.getCard().getId())).isNotNull();
        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void minusThreeCannotTargetLand() {
        Permanent kaya = addReadyKaya(5);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new SnowCoveredPlains());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(kaya), 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void plusOneCannotTargetTokenCreature() {
        Permanent kaya = addReadyKaya(5);
        Permanent tyvar = harness.addToBattlefieldAndReturn(player1, new TyvarKell());
        tyvar.setCounterCount(CounterType.LOYALTY, 3);
        harness.activateAbility(player1, battlefieldIndex(tyvar), 1, null, null);
        resolveAllTriggers();
        Permanent elf = findPermanent(player1, "Elf Warrior");

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(kaya), 0, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleGhostformAbilitiesEachCreateASpirit() {
        Permanent kaya = addReadyKaya(5);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, battlefieldIndex(kaya), 0, null, creature.getId());
        resolveAllTriggers();
        harness.performUntapStep(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, battlefieldIndex(kaya), 0, null, creature.getId());
        resolveAllTriggers();

        harness.setHand(player1, List.of(new IonasJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
    }

    @Test
    void ghostformAbilityWorksAfterItsCounterIsRemoved() {
        Permanent kaya = addReadyKaya(5);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, battlefieldIndex(kaya), 0, null, creature.getId());
        resolveAllTriggers();
        creature.setCounterCount(CounterType.GHOSTFORM, 0);

        harness.setHand(player1, List.of(new IonasJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    @Test
    void ghostformOnOpposingCreatureCreatesSpiritForItsController() {
        Permanent kaya = addReadyKaya(5);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.activateAbility(player1, battlefieldIndex(kaya), 0, null, creature.getId());
        resolveAllTriggers();

        harness.setHand(player1, List.of(new IonasJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(countPermanents(player2, "Spirit")).isEqualTo(1);
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    void ultimateCastsLegendarySpellFromGraveyard() {
        createEmblem();
        harness.setGraveyard(player1, List.of(new TyvarKell()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Tyvar Kell");
        harness.assertNotInGraveyard(player1, "Tyvar Kell");
    }

    @Test
    void ultimateCastsLegendarySpellFromExile() {
        createEmblem();
        TyvarKell tyvar = new TyvarKell();
        harness.setExile(player1, List.of(tyvar));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Tyvar Kell");
        assertThat(gd.findExiledCard(tyvar.getId())).isNull();
    }

    @Test
    void ultimateDoesNotUseCardsOwnedByOpponent() {
        createEmblem();
        harness.setExile(player2, List.of(new TyvarKell()));
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Tyvar Kell");
    }

    @Test
    void ultimateDoesNotOfferNonlegendaryCards() {
        createEmblem();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new SnowCoveredPlains()));
        harness.setExile(player1, List.of(new TyvarKell()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Tyvar Kell");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Snow-Covered Plains");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void ultimateDoesNotTriggerDuringOpponentsUpkeep() {
        createEmblem();
        harness.setHand(player1, List.of(new TyvarKell()));
        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Tyvar Kell");
    }

    @Test
    void ultimateAllowsOnlyOneSpellAcrossAllZones() {
        createEmblem();
        harness.setHand(player1, List.of(new TyvarKell()));
        harness.setGraveyard(player1, List.of(new KayaTheInexorable()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Tyvar Kell");
        harness.assertNotOnBattlefield(player1, "Kaya the Inexorable");
        harness.assertInGraveyard(player1, "Kaya the Inexorable");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void ultimateCanCastFaceDownLegendaryCardWithLookPermission() {
        createEmblem();
        TyvarKell tyvar = new TyvarKell();
        gd.addToExile(player1.getId(), tyvar, null, true, player2.getId());
        gd.additionalExileLookPermissions.put(tyvar.getId(), Set.of(player1.getId()));
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Tyvar Kell");
    }

    @Test
    void decliningHandSpellDoesNotRevealItsIdentity() {
        createEmblem();
        harness.setHand(player1, List.of(new TyvarKell()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertInHand(player1, "Tyvar Kell");
        assertThat(gameLogContains("declines to cast Tyvar Kell")).isFalse();
    }

    private void createEmblem() {
        Permanent kaya = addReadyKaya(7);
        harness.activateAbility(player1, battlefieldIndex(kaya), 2, null, null);
        resolveAllTriggers();
        harness.setGraveyard(player1, List.of());
    }

    private Permanent addReadyKaya(int loyalty) {
        Permanent kaya = harness.addToBattlefieldAndReturn(player1, new KayaTheInexorable());
        kaya.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return kaya;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
