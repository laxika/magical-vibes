package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrbanRetreat.class, GrizzlyBears.class})
class UrbanRetreatTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and produces one of three colors")
    void entersTappedAndProducesChosenMana() {
        UrbanRetreat retreatCard = new UrbanRetreat();
        harness.setHand(player1, List.of(retreatCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        Permanent retreat = findPermanent(player1, "Urban Retreat");
        assertThat(retreat.isTapped()).isTrue();

        retreat.untap();
        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("GREEN", "WHITE", "BLUE");

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(retreat.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Returns a tapped creature as a cost and puts itself onto the battlefield tapped")
    void returnsTappedCreatureAndReturnsFromHand() {
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        tappedCreature.tap();
        Permanent secondTappedCreature = addCreatureReady(player1, new GrizzlyBears());
        secondTappedCreature.tap();
        Permanent untappedCreature = addCreatureReady(player1, new GrizzlyBears());
        UrbanRetreat retreatCard = new UrbanRetreat();
        harness.setHand(player1, List.of(retreatCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);

        harness.activateHandAbility(player1, 0, null);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(tappedCreature.getId(), secondTappedCreature.getId());
        assertThat(choice.validIds()).doesNotContain(untappedCreature.getId());

        harness.handlePermanentChosen(player1, tappedCreature.getId());

        assertThat(gd.playerHands.get(player1.getId())).contains(retreatCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tappedCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(untappedCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondTappedCreature);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(retreatCard));

        Permanent enteredRetreat = findPermanent(player1, "Urban Retreat");
        assertThat(enteredRetreat.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(tappedCreature.getCard());
    }

    @Test
    void puttingTheSourceOntoTheBattlefieldCannotBeDeclined() {
        addCreatureReady(player1, new GrizzlyBears()).tap();
        addCreatureReady(player1, new GrizzlyBears()).tap();
        UrbanRetreat card = new UrbanRetreat();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateHandAbility(player1, 0, null);
        harness.handlePermanentChosen(player1, gd.playerBattlefields.get(player1.getId()).getFirst().getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(card));
        harness.assertOnBattlefield(player1, "Urban Retreat");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(card);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"GREEN", "WHITE", "BLUE"})
    void producesExactlyOneManaWithoutUsingTheStack(ManaColor color) {
        harness.addToBattlefield(player1, new UrbanRetreat());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        for (ManaColor poolColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(poolColor))
                    .isEqualTo(poolColor == color ? 1 : 0);
        }
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Urban Retreat").isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutATappedCreatureYouControl() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears()).tap();
        harness.addToBattlefieldAndReturn(player1, new UrbanRetreat()).tap();
        UrbanRetreat card = new UrbanRetreat();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutTwoMana() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        UrbanRetreat card = new UrbanRetreat();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateOutsideYourMainPhase() {
        addCreatureReady(player1, new GrizzlyBears()).tap();
        harness.setHand(player1, List.of(new UrbanRetreat()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void handAbilityWorksAfterPlayingALandAndCannotBeActivatedWithANonemptyStack() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        addCreatureReady(player1, new GrizzlyBears()).tap();
        UrbanRetreat first = new UrbanRetreat();
        UrbanRetreat second = new UrbanRetreat();
        UrbanRetreat third = new UrbanRetreat();
        harness.setHand(player1, List.of(first, second, third));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, null);
        harness.handlePermanentChosen(player1, creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 1, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(second));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() == second).singleElement()
                .satisfies(p -> assertThat(p.isTapped()).isTrue());
        assertThat(gd.playerHands.get(player1.getId())).contains(third, creature.getCard()).doesNotContain(second);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }
}
