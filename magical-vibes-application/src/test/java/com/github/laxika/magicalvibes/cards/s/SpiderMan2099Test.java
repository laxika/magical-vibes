package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.cards.y.YawgmothsWill;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiderMan2099.class, ThinkTwice.class, YawgmothsWill.class, Forest.class})
class SpiderMan2099Test extends BaseCardTest {

    @Test
    @DisplayName("Cannot be cast during the controller's first three turns")
    void cannotBeCastDuringFirstThreeTurns() {
        gd.turnsTakenByPlayer.put(player1.getId(), 3);
        harness.setHand(player1, List.of(new SpiderMan2099()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can be cast during the controller's fourth turn")
    void canBeCastDuringFourthTurn() {
        gd.turnsTakenByPlayer.put(player1.getId(), 4);
        harness.setHand(player1, List.of(new SpiderMan2099()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spider-Man 2099");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("Cannot be cast during either of the first two controller turns")
    void cannotBeCastDuringFirstTwoTurns(int turnCount) {
        gd.turnsTakenByPlayer.put(player1.getId(), turnCount);
        harness.setHand(player1, List.of(new SpiderMan2099()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Deals damage equal to its power after a spell was cast from outside the hand")
    void dealsDamageAfterCastingFromOutsideHand() {
        harness.addToBattlefield(player1, new SpiderMan2099());
        harness.setHand(player1, List.of(new YawgmothsWill()));
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger after only casting a spell from hand")
    void doesNotTriggerAfterHandCast() {
        harness.addToBattlefield(player1, new SpiderMan2099());
        harness.setHand(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);
        advanceToEndStep();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Playing a land from the graveyard enables the end-step trigger")
    void triggersAfterPlayingLandFromGraveyard() {
        harness.addToBattlefield(player1, new SpiderMan2099());
        harness.setHand(player1, List.of(new YawgmothsWill()));
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.playLandFromGraveyard(player1, 0);

        advanceToEndStep();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Playing a land from hand does not enable the end-step trigger")
    void doesNotTriggerAfterPlayingLandFromHand() {
        harness.addToBattlefield(player1, new SpiderMan2099());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        advanceToEndStep();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage uses power at resolution rather than power when the ability triggered")
    void damageUsesPowerAtResolution() {
        Permanent spiderMan = harness.addToBattlefieldAndReturn(player1, new SpiderMan2099());
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep();
        harness.handlePermanentChosen(player1, player2.getId());
        spiderMan.setPowerModifier(3);
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Several spells cast outside hand still produce only one end-step trigger")
    void triggersOnlyOnceForSeveralOutsideHandCasts() {
        harness.addToBattlefield(player1, new SpiderMan2099());
        harness.setGraveyard(player1, List.of(new ThinkTwice(), new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep();
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's spell cast outside hand does not enable the controller's trigger")
    void doesNotTriggerForOpponentsOutsideHandCast() {
        harness.addToBattlefield(player1, new SpiderMan2099());
        harness.setGraveyard(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player2);
        harness.castFlashback(player2, 0);
        harness.passBothPriorities();

        advanceToEndStep();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("End-step damage may target a creature")
    void damageCanTargetCreature() {
        harness.addToBattlefield(player1, new SpiderMan2099());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpiderMan2099());
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Spider-Man 2099");
        harness.assertLife(player2, 20);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
