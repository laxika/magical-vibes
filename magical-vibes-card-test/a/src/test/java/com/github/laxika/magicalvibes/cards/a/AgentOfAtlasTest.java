package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgentOfAtlas.class, GrizzlyBears.class, Shock.class})
class AgentOfAtlasTest extends BaseCardTest {

    @Test
    @DisplayName("Prowess gives Agent of Atlas +1/+1 for a noncreature spell")
    void noncreatureSpellPumps() {
        Permanent agent = addAgent();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, agent)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, agent)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prowess does not trigger for a creature spell")
    void creatureSpellDoesNotPump() {
        Permanent agent = addAgent();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, agent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, agent)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prowess wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent agent = addAgent();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, agent)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, agent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, agent)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prowess resolves before the spell that triggered it")
    void prowessResolvesBeforeTriggeringSpell() {
        Permanent agent = addAgent();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, agent.getId());

        assertThat(gqs.getEffectiveToughness(gd, agent)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, agent)).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(agent);
        assertThat(gqs.getEffectivePower(gd, agent)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each noncreature spell grants another prowess boost")
    void repeatedCastsStackBoosts() {
        Permanent agent = addAgent();

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, agent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, agent)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger prowess")
    void opponentSpellDoesNotPump() {
        Permanent agent = addAgent();
        harness.forceActivePlayer(player2);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, agent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, agent)).isEqualTo(2);
    }

    private Permanent addAgent() {
        Permanent agent = harness.addToBattlefieldAndReturn(player1, new AgentOfAtlas());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return agent;
    }
}
