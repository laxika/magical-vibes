package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.h.HoodedKavu;
import com.github.laxika.magicalvibes.cards.y.YavimayaBarbarian;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CauldronDance.class, HolyDay.class, HoodedKavu.class, YavimayaBarbarian.class})
class CauldronDanceTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a hasty graveyard creature and returns it to hand at the next end step")
    void reanimatesAndReturnsCreatureAtEndStep() {
        YavimayaBarbarian graveyardCreature = new YavimayaBarbarian();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new CauldronDance()));
        addCauldronDanceMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player1, 0, graveyardCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        var returned = findPermanent(player1, "Yavimaya Barbarian");
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(returned.getId())
                        && action.kind() == DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Yavimaya Barbarian");
        harness.assertInHand(player1, "Yavimaya Barbarian");
    }

    @Test
    @DisplayName("Puts a hand creature onto the battlefield hasty and sacrifices it at the next end step")
    void putsHandCreatureAndSacrificesAtEndStep() {
        YavimayaBarbarian graveyardCreature = new YavimayaBarbarian();
        HoodedKavu handCreature = new HoodedKavu();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new CauldronDance(), handCreature));
        addCauldronDanceMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player1, 0, graveyardCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        var handPermanent = findPermanent(player1, "Hooded Kavu");
        assertThat(handPermanent.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(handPermanent.getId())
                        && action.kind() == DelayedPermanentActionKind.SACRIFICE_AT_END_STEP);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(handCreature.getId()));
        harness.assertInGraveyard(player1, "Hooded Kavu");
    }

    @Test
    @DisplayName("May decline putting a creature from hand onto the battlefield")
    void mayDeclinePuttingHandCreature() {
        YavimayaBarbarian graveyardCreature = new YavimayaBarbarian();
        HoodedKavu handCreature = new HoodedKavu();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new CauldronDance(), handCreature));
        addCauldronDanceMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player1, 0, graveyardCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Yavimaya Barbarian");
        harness.assertInHand(player1, "Hooded Kavu");
        harness.assertNotOnBattlefield(player1, "Hooded Kavu");
    }

    @Test
    @DisplayName("Cannot be cast outside combat")
    void cannotCastOutsideCombat() {
        YavimayaBarbarian graveyardCreature = new YavimayaBarbarian();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new CauldronDance()));
        addCauldronDanceMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, graveyardCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNonCreatureCard() {
        HolyDay noncreature = new HolyDay();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setHand(player1, List.of(new CauldronDance()));
        addCauldronDanceMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        YavimayaBarbarian opponentCreature = new YavimayaBarbarian();
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new CauldronDance()));
        addCauldronDanceMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addCauldronDanceMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
