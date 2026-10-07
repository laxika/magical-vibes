package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TalonGatesOfMadara.class, SakuraTribeElder.class})
class TalonGatesOfMadaraTest extends BaseCardTest {

    @Test
    @DisplayName("Its ETB phases out up to one target creature")
    void etbPhasesOutTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SakuraTribeElder());
        harness.setHand(player1, List.of(new TalonGatesOfMadara()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Produces colorless mana and one mana of any color")
    void producesBothManaAbilities() {
        harness.setHand(player1, List.of(new TalonGatesOfMadara()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        Permanent land = findPermanent(player1, "Talon Gates of Madara");
        int landIndex = gd.playerBattlefields.get(player1.getId()).indexOf(land);

        harness.activateAbility(player1, landIndex, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        land.untap();
        harness.activateAbility(player1, landIndex, 1, null, null);
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Can put itself from hand onto the battlefield")
    void putsItselfFromHandOntoBattlefield() {
        TalonGatesOfMadara card = new TalonGatesOfMadara();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.playerHands.get(player1.getId())).contains(card);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(card);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == card);
    }

    @Test
    @DisplayName("May choose no creature even when a creature is available")
    void mayDeclineEtbTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SakuraTribeElder());
        harness.enterBattlefieldAndReturn(player1, new TalonGatesOfMadara());

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An owned creature phases back in only at its controller's untap step")
    void ownCreaturePhasesInAtControllersUntap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SakuraTribeElder());
        harness.enterBattlefieldAndReturn(player1, new TalonGatesOfMadara());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.performUntapStep(player2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Hand activation on the opponent's turn puts only its source into play and triggers phasing")
    void handAbilityWorksOnOpponentsTurnAndTriggersEtb() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SakuraTribeElder());
        TalonGatesOfMadara otherCopy = new TalonGatesOfMadara();
        TalonGatesOfMadara source = new TalonGatesOfMadara();
        harness.setHand(player1, List.of(otherCopy, source));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.ensurePriority(player1);

        harness.activateHandAbility(player1, 1, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        PendingInteraction.HandCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(otherCopy);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == source && !permanent.isTapped());
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Resolving the hand ability cannot be declined")
    void handAbilityCannotBeDeclined() {
        TalonGatesOfMadara card = new TalonGatesOfMadara();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(card);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == card);
    }
}
