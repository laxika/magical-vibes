package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WingshieldAgent.class, GrizzlyBears.class, Shock.class, Murder.class})
class WingshieldAgentTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a shield counter")
    void entersWithShieldCounter() {
        Permanent agent = castAgent();

        assertThat(agent.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    @DisplayName("Its shield counter prevents one damage event")
    void shieldCounterPreventsDamage() {
        Permanent agent = castAgent();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, agent.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(agent);
        assertThat(agent.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(agent.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Attacking gives another creature flying until end of turn")
    void attackingGivesAnotherCreatureFlying() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WingshieldAgent());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        harness.handlePermanentChosen(player1, other.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isTrue();

        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetItself() {
        harness.setLife(player2, 20);
        Permanent agent = addCreatureReady(player1, new WingshieldAgent());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, agent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castAgent() {
        harness.setHand(player1, List.of(new WingshieldAgent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Wingshield Agent");
    }

    @Test
    @DisplayName("A shield prevents destruction once, then the creature can be destroyed")
    void shieldPreventsOnlyFirstDestruction() {
        Permanent agent = castAgent();
        harness.setHand(player1, List.of(new Murder(), new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, agent.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(agent);
        assertThat(agent.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(agent.isTapped()).isFalse();

        harness.castInstant(player1, 0, agent.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(agent);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(agent.getCard());
    }

    @Test
    @DisplayName("Attacking can give another creature you control flying")
    void canTargetAnotherFriendlyCreature() {
        Permanent agent = addCreatureReady(player1, new WingshieldAgent());
        Permanent other = addCreatureReady(player1, new WingshieldAgent());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, other.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, agent, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Attacking may choose no target even when another creature is available")
    void canChooseNoTarget() {
        Permanent agent = addCreatureReady(player1, new WingshieldAgent());
        Permanent other = addCreatureReady(player1, new WingshieldAgent());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, agent, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attacking alone needs no other creature to target")
    void canAttackWithNoOtherCreature() {
        Permanent agent = addCreatureReady(player1, new WingshieldAgent());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, agent, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
