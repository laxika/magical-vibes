package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FlywheelRacer;
import com.github.laxika.magicalvibes.cards.i.InvasionOfGobakhan;
import com.github.laxika.magicalvibes.cards.l.LightshieldArray;
import com.github.laxika.magicalvibes.cards.s.Stifle;
import com.github.laxika.magicalvibes.cards.s.SwordswornCavalier;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AyaraWidowOfTheRealm.class, AyaraFurnaceQueen.class, SwordswornCavalier.class,
        InvasionOfGobakhan.class, LightshieldArray.class, FlywheelRacer.class, Stifle.class})
class AyaraWidowOfTheRealmTest extends BaseCardTest {

    @Test
    void sacrificeAbilityUsesSacrificedManaValueForDamageAndLife() {
        Permanent ayara = addCreatureReady(player1, new AyaraWidowOfTheRealm());
        harness.addToBattlefield(player1, new SwordswornCavalier());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ayara), null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Swordsworn Cavalier");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void damageAbilityCanTargetBattleButNotCreature() {
        Permanent ayara = addCreatureReady(player1, new AyaraWidowOfTheRealm());
        harness.addToBattlefield(player1, new SwordswornCavalier());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfGobakhan());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int defenseBefore = battle.getCounterCount(CounterType.DEFENSE);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ayara), null, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(defenseBefore - 2);

        ayara.untap();
        harness.addToBattlefield(player1, new SwordswornCavalier());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SwordswornCavalier());
        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(ayara), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void transformAbilityUsesSorceryTiming() {
        Permanent ayara = harness.addToBattlefieldAndReturn(player1, new AyaraWidowOfTheRealm());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ayara), 1, null, null);
        harness.passBothPriorities();

        assertThat(ayara.isTransformed()).isTrue();
    }

    @Test
    void furnaceQueenReturnsUpToOneArtifactOrCreatureWithHasteAndExilesItAtEndStep() {
        Card target = new SwordswornCavalier();
        harness.setGraveyard(player1, List.of(target));
        Permanent ayara = harness.addToBattlefieldAndReturn(player1, new AyaraWidowOfTheRealm());
        ayara.setCard(ayara.getCard().getBackFaceCard());
        ayara.setTransformed(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Swordsworn Cavalier");

        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);
        declareAttackers(player1, List.of());
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Swordsworn Cavalier");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
    }

    @Test
    void canSacrificeANoncreatureArtifactWithoutManaPayment() {
        Permanent ayara = addCreatureReady(player1, new AyaraWidowOfTheRealm());
        harness.addToBattlefield(player1, new FlywheelRacer());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Flywheel Racer");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(ayara.isTapped()).isTrue();
    }

    @Test
    void cannotSacrificeItselfWhenThereIsNoOtherCreatureOrArtifact() {
        Permanent ayara = addCreatureReady(player1, new AyaraWidowOfTheRealm());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Ayara, Widow of the Realm");
        assertThat(ayara.isTapped()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotTargetItsController() {
        addCreatureReady(player1, new AyaraWidowOfTheRealm());
        harness.addToBattlefield(player1, new FlywheelRacer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Flywheel Racer");
    }

    @Test
    void sacrificedTransformedPermanentUsesItsFrontFaceManaValue() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AyaraWidowOfTheRealm());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(sacrifice.isTransformed()).isTrue();
        Permanent ayara = addCreatureReady(player1, new AyaraWidowOfTheRealm());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ayara),
                null, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ayara, Furnace Queen");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void transformCanPayTwoLifeInsteadOfRedMana() {
        Permanent ayara = harness.addToBattlefieldAndReturn(player1, new AyaraWidowOfTheRealm());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(ayara.isTransformed()).isTrue();
        harness.assertLife(player1, 18);
    }

    @Test
    void cannotTransformOutsideAMainPhase() {
        Permanent ayara = harness.addToBattlefieldAndReturn(player1, new AyaraWidowOfTheRealm());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ayara.isTransformed()).isFalse();
    }

    @Test
    void furnaceQueenCanReturnANoncreatureArtifact() {
        Card target = new FlywheelRacer();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new AyaraFurnaceQueen());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Flywheel Racer");
        harness.assertNotInGraveyard(player1, "Flywheel Racer");
        declareAttackers(player1, List.of());
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Flywheel Racer");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
    }

    @Test
    void furnaceQueenCanChooseNoTargetEvenWithAnEligibleCard() {
        Card target = new FlywheelRacer();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new AyaraFurnaceQueen());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Flywheel Racer");
        harness.assertNotOnBattlefield(player1, "Flywheel Racer");
    }

    @Test
    void furnaceQueenDoesNotTriggerOnAnOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new FlywheelRacer()));
        harness.addToBattlefield(player1, new AyaraFurnaceQueen());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Flywheel Racer");
    }

    @Test
    void furnaceQueenCannotReturnATargetThatLeavesTheGraveyardInResponse() {
        Card target = new FlywheelRacer();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new AyaraFurnaceQueen());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Flywheel Racer");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
    }

    @Test
    void hastePersistsAfterCleanupIfTheDelayedExileIsCountered() {
        Card target = new FlywheelRacer();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new AyaraFurnaceQueen());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Flywheel Racer");
        declareAttackers(player1, List.of());
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.setHand(player1, List.of(new Stifle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, gd.stack.getLast().getCard().getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.assertOnBattlefield(player1, "Flywheel Racer");
        assertThat(returned.hasKeyword(Keyword.HASTE)).isTrue();
    }
}
