package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheRavenMan.class, GrizzlyBears.class})
class TheRavenManTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent discarding a card creates a 1/1 flying Bird at the end step")
    void opponentDiscardCreatesBirdAtEndStep() {
        Permanent raven = addReadyRaven();
        Card discarded = new GrizzlyBears();
        harness.setHand(player2, List.of(discarded));
        addManaForAbility();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(raven), null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);

        advanceToEndStep();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent bird = findPermanent(player1, "Bird");
        assertThat(bird.getEffectivePower()).isEqualTo(1);
        assertThat(bird.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bird, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when no player discarded a card this turn")
    void doesNotTriggerWithoutDiscard() {
        addReadyRaven();

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Bird")).isEmpty();
    }

    @Test
    @DisplayName("The Bird token cannot block")
    void birdTokenCannotBlock() {
        Permanent bird = createBirdAfterOpponentDiscard();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        int birdIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bird);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(birdIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The discard ability can only be activated at sorcery speed")
    void discardAbilityIsSorcerySpeedOnly() {
        Permanent raven = addReadyRaven();
        addManaForAbility();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(raven), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReadyRaven() {
        Permanent raven = addCreatureReady(player1, new TheRavenMan());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return raven;
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
    }

    private Permanent createBirdAfterOpponentDiscard() {
        Permanent raven = addReadyRaven();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        addManaForAbility();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(raven), null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        advanceToEndStep();
        harness.passBothPriorities();
        return findPermanent(player1, "Bird");
    }
}
