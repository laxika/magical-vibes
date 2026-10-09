package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Blossombind;
import com.github.laxika.magicalvibes.cards.c.ChampionOfTheWeird;
import com.github.laxika.magicalvibes.cards.s.SizzlingChangeling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({DawnhandDissident.class, Blossombind.class, ChampionOfTheWeird.class, SizzlingChangeling.class})
class DawnhandDissidentTest extends BaseCardTest {

    @Test
    void blightOneSurveilsTheTopCard() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        Card topCard = new SizzlingChangeling();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(dawnhand.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void blightTwoExilesTheTargetFromAGraveyard() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        Permanent costCreature = addCreatureReady(player1, new ChampionOfTheWeird());
        Card target = new SizzlingChangeling();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, costCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(dawnhand.getId())).contains(target);
        assertThat(costCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    void castsOwnedCreatureExiledWithDawnhandByRemovingCounters() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        Permanent costCreature = addCreatureReady(player1, new ChampionOfTheWeird());
        costCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Card exiledCreature = new SizzlingChangeling();
        gd.addToExile(player1.getId(), exiledCreature, dawnhand.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromExile(player1, exiledCreature.getId(),
                List.of(costCreature.getId(), costCreature.getId(), costCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() == exiledCreature);
        assertThat(costCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getCardsExiledByPermanent(dawnhand.getId())).isEmpty();
    }

    @Test
    void cannotCastNoncreatureExiledWithDawnhand() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        Card noncreature = new Blossombind();
        gd.addToExile(player1.getId(), noncreature, dawnhand.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
    }

    @Test
    void surveilCanLeaveTheCardOnTop() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        Card topCard = new DawnhandDissident();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
        assertThat(dawnhand.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(dawnhand.isTapped()).isTrue();
    }

    @Test
    void canExileANoncreatureFromTheOpponentsGraveyard() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        Permanent costCreature = addCreatureReady(player1, new ChampionOfTheWeird());
        Card target = new Blossombind();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);
        harness.handlePermanentChosen(player1, costCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getCardsExiledByPermanent(dawnhand.getId())).contains(target);
        assertThat(costCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotBlightACreatureThatCannotReceiveCounters() {
        addCreatureReady(player1, new DawnhandDissident());
        Permanent costCreature = addCreatureReady(player1, new SizzlingChangeling());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Blossombind());
        aura.setAttachedTo(costCreature.getId());
        harness.setLibrary(player1, List.of(new DawnhandDissident()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, costCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(costCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void cannotActivateWhenNoCreatureCanReceiveBlightCounters() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Blossombind());
        aura.setAttachedTo(dawnhand.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dawnhand.isTapped()).isFalse();
    }

    @Test
    void canRemoveCountersOfDifferentTypesAcrossCreatures() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        Permanent otherCreature = addCreatureReady(player1, new SizzlingChangeling());
        dawnhand.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        otherCreature.setCounterCount(CounterType.CHARGE, 2);
        Card exiledCreature = new DawnhandDissident();
        gd.addToExile(player1.getId(), exiledCreature, dawnhand.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromExile(player1, exiledCreature.getId(),
                List.of(dawnhand.getId(), otherCreature.getId(), otherCreature.getId()));
        harness.passBothPriorities();

        assertThat(dawnhand.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(otherCreature.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == exiledCreature);
    }

    @Test
    void asksWhichCounterTypesToRemoveWhenThePaymentIsAmbiguous() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        Permanent costCreature = addCreatureReady(player1, new SizzlingChangeling());
        costCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        costCreature.setCounterCount(CounterType.CHARGE, 3);
        Card exiledCreature = new DawnhandDissident();
        gd.addToExile(player1.getId(), exiledCreature, dawnhand.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromExile(player1, exiledCreature.getId(),
                List.of(costCreature.getId(), costCreature.getId(), costCreature.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(costCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(costCreature.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        harness.handleListChoice(player1, CounterType.CHARGE.name());
        harness.handleListChoice(player1, CounterType.CHARGE.name());
        harness.handleListChoice(player1, CounterType.CHARGE.name());

        assertThat(costCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(costCreature.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void permitsExiledCreatureWithAnAdditionalBeholdCost() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        Permanent counterCreature = addCreatureReady(player1, new DawnhandDissident());
        counterCreature.setCounterCount(CounterType.CHARGE, 3);
        Permanent beheld = addCreatureReady(player1, new SizzlingChangeling());
        Card exiledCreature = new ChampionOfTheWeird();
        gd.addToExile(player1.getId(), exiledCreature, dawnhand.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatCode(() -> harness.castFromExile(player1, exiledCreature.getId(),
                List.of(counterCreature.getId(), counterCreature.getId(), counterCreature.getId())))
                .doesNotThrowAnyException();
        assertThat(gd.interaction.isAwaitingInput() || !gd.stack.isEmpty()).isTrue();
        harness.handlePermanentChosen(player1, beheld.getId());

        assertThat(counterCreature.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.findExiledCard(beheld.getCard().getId())).isNotNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getBeheldCard()).isEqualTo(beheld.getOriginalCard());
    }

    @Test
    void cannotCastAnOpponentsExiledCreature() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        dawnhand.setCounterCount(CounterType.CHARGE, 3);
        Card exiledCreature = new DawnhandDissident();
        gd.addToExile(player2.getId(), exiledCreature, dawnhand.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiledCreature.getId(),
                List.of(dawnhand.getId(), dawnhand.getId(), dawnhand.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
        assertThat(dawnhand.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void cannotCastDuringTheOpponentsTurn() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        dawnhand.setCounterCount(CounterType.CHARGE, 3);
        Card exiledCreature = new DawnhandDissident();
        gd.addToExile(player1.getId(), exiledCreature, dawnhand.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiledCreature.getId(),
                List.of(dawnhand.getId(), dawnhand.getId(), dawnhand.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
        assertThat(dawnhand.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void cannotCastWithoutThreeCounters() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        dawnhand.setCounterCount(CounterType.CHARGE, 2);
        Card exiledCreature = new DawnhandDissident();
        gd.addToExile(player1.getId(), exiledCreature, dawnhand.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiledCreature.getId(),
                List.of(dawnhand.getId(), dawnhand.getId(), dawnhand.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
        assertThat(dawnhand.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void cannotCastOutsideTheMainPhaseWithoutFlash() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        dawnhand.setCounterCount(CounterType.CHARGE, 3);
        Card exiledCreature = new DawnhandDissident();
        gd.addToExile(player1.getId(), exiledCreature, dawnhand.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiledCreature.getId(),
                List.of(dawnhand.getId(), dawnhand.getId(), dawnhand.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
        assertThat(dawnhand.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void permissionEndsWhenDawnhandLeavesTheBattlefield() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        Permanent counterCreature = addCreatureReady(player1, new DawnhandDissident());
        counterCreature.setCounterCount(CounterType.CHARGE, 3);
        Card exiledCreature = new DawnhandDissident();
        gd.addToExile(player1.getId(), exiledCreature, dawnhand.getId());
        gd.playerBattlefields.get(player1.getId()).remove(dawnhand);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiledCreature.getId(),
                List.of(counterCreature.getId(), counterCreature.getId(), counterCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
        assertThat(counterCreature.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void exileAbilityDoesNothingIfItsTargetLeavesTheGraveyard() {
        Permanent dawnhand = addCreatureReady(player1, new DawnhandDissident());
        Permanent costCreature = addCreatureReady(player1, new ChampionOfTheWeird());
        Card target = new DawnhandDissident();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);
        harness.handlePermanentChosen(player1, costCreature.getId());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(dawnhand.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(costCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(dawnhand.isTapped()).isTrue();
    }
}
