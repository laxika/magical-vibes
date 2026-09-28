package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MBakuJabariChieftain.class, GrizzlyBears.class})
class MBakuJabariChieftainTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of its controller's end step, targets an opponent to become monarch")
    void targetOpponentBecomesMonarchWhenThereIsNoMonarch() {
        harness.addToBattlefield(player1, new MBakuJabariChieftain());

        advanceToPlayer1EndStep();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The end-step ability does not trigger while a monarch exists")
    void doesNotTriggerWhenThereIsAlreadyAMonarch() {
        harness.addToBattlefield(player1, new MBakuJabariChieftain());
        gd.monarchPlayerId = player2.getId();

        advanceToPlayer1EndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An attacker gets +1/+1 and trample when attacking the monarch")
    void boostsCreatureAttackingTheMonarch() {
        harness.addToBattlefield(player1, new MBakuJabariChieftain());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        gd.monarchPlayerId = player2.getId();

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The attack ability does not trigger when the attacked player is not the monarch")
    void doesNotBoostWhenAttackedPlayerIsNotMonarch() {
        harness.addToBattlefield(player1, new MBakuJabariChieftain());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        gd.monarchPlayerId = player1.getId();

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();
    }

    private void advanceToPlayer1EndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
