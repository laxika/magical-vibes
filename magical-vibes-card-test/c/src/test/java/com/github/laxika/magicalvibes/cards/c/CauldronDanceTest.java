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

        harness.castAndResolveInstant(player1, 0, graveyardCreature.getId());
        harness.handleMayAbilityChosen(player1, false);

        var returned = findPermanent(player1, "Yavimaya Barbarian");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(returned.getId())
                        && action.kind() == DelayedPermanentActionKind.RETURN_TO_HAND_AT_END_STEP);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

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

        harness.castAndResolveInstant(player1, 0, graveyardCreature.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        var handPermanent = findPermanent(player1, "Hooded Kavu");
        assertThat(gqs.hasKeyword(gd, handPermanent, Keyword.HASTE)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

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

        harness.castAndResolveInstant(player1, 0, graveyardCreature.getId());
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

    @Test
    @DisplayName("Both end-step abilities use the stack before either creature leaves")
    void endStepAbilitiesAllowResponses() {
        YavimayaBarbarian graveyardCreature = new YavimayaBarbarian();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new CauldronDance(), new HoodedKavu()));
        addCauldronDanceMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0, graveyardCreature.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Yavimaya Barbarian");
        harness.assertOnBattlefield(player1, "Hooded Kavu");
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Yavimaya Barbarian");
        harness.assertInGraveyard(player1, "Hooded Kavu");
    }

    @Test
    @DisplayName("An illegal graveyard target prevents the optional hand creature from entering")
    void illegalTargetStopsEntireSpell() {
        YavimayaBarbarian graveyardCreature = new YavimayaBarbarian();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new CauldronDance(), new HoodedKavu()));
        addCauldronDanceMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player1, 0, graveyardCreature.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Yavimaya Barbarian");
        harness.assertNotOnBattlefield(player1, "Hooded Kavu");
        harness.assertInHand(player1, "Hooded Kavu");
        harness.assertInGraveyard(player1, "Cauldron Dance");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Noncreature cards in hand cannot be put onto the battlefield")
    void noEligibleCreatureInHandStillReturnsGraveyardCreature() {
        YavimayaBarbarian graveyardCreature = new YavimayaBarbarian();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new CauldronDance(), new HolyDay()));
        addCauldronDanceMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player1, 0, graveyardCreature.getId());
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player1, "Yavimaya Barbarian");
        harness.assertInHand(player1, "Holy Day");
        harness.assertNotOnBattlefield(player1, "Holy Day");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void addCauldronDanceMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
