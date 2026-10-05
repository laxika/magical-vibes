package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({PracticedOffense.class, GrizzlyBears.class})
class PracticedOffenseTest extends BaseCardTest {

    @Test
    @DisplayName("Puts +1/+1 on each creature target player controls and grants chosen keyword")
    void buffsPlayerCreaturesAndGrantsKeyword() {
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PracticedOffense()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), target.getId()));
        harness.handleListChoice(player1, "DOUBLE_STRIKE");

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Lifelink can target an opposing creature independently of the player receiving counters")
    void grantsLifelinkToOpponentAndExpiresAtCleanup() {
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PracticedOffense()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), target.getId()));
        harness.handleListChoice(player1, "LIFELINK");

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.LIFELINK)).isFalse();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can put counters on an opponent's creatures while granting double strike to your creature")
    void targetsOpponentForCounters() {
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherOpponent = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PracticedOffense()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), ally.getId()));
        harness.handleListChoice(player1, "DOUBLE_STRIKE");

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherOpponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Flashback resolves both effects for two mana and exiles the spell")
    void flashbackResolvesAndExiles() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new PracticedOffense()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, List.of(player1.getId(), target.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "LIFELINK");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
        harness.assertNotInGraveyard(player1, "Practiced Offense");
        assertThat(gd.exiledCards)
                .anyMatch(exiled -> exiled.card() instanceof PracticedOffense);
    }

    @Test
    @DisplayName("Counters still resolve when the creature target has left the battlefield")
    void missingCreatureTargetDoesNotPreventCounters() {
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PracticedOffense()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, List.of(player1.getId(), target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Practiced Offense");
    }
}
