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

    @Test
    @DisplayName("Creatures attacking M'Baku's controller do not receive its bonus")
    void doesNotBoostCreatureAttackingItsControllerAsMonarch() {
        harness.addToBattlefield(player1, new MBakuJabariChieftain());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        gd.monarchPlayerId = player1.getId();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The attacked player must still be monarch when the attack trigger resolves")
    void doesNotBoostIfMonarchChangesBeforeResolution() {
        Permanent attacker = addCreatureReady(player1, new MBakuJabariChieftain());
        gd.monarchPlayerId = player2.getId();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.monarchPlayerId = player1.getId();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The end-step trigger does nothing if a monarch appears before resolution")
    void rechecksNoMonarchAtResolution() {
        harness.addToBattlefield(player1, new MBakuJabariChieftain());
        advanceToPlayer1EndStep();
        harness.handlePermanentChosen(player1, player2.getId());
        gd.monarchPlayerId = player1.getId();

        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The end-step ability does not trigger during an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new MBakuJabariChieftain());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.monarchPlayerId).isNull();
    }

    private void advanceToPlayer1EndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
