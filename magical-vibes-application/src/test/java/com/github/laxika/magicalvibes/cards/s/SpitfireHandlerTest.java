package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpitfireHandler.class, GrizzlyBears.class})
class SpitfireHandlerTest extends BaseCardTest {

    private static final String DENIAL = "This creature can't block creatures with power greater than this creature's power";

    @Test
    @DisplayName("A creature with greater power can't be blocked by Spitfire Handler")
    void greaterPowerCreatureCannotBeBlocked() {
        addReadySpitfireHandler(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(DENIAL);
    }

    @Test
    @DisplayName("A creature with equal power can be blocked by Spitfire Handler")
    void equalPowerCreatureCanBeBlocked() {
        addReadySpitfireHandler(player2);
        Permanent attacker = addCreatureReady(player1, new SpitfireHandler());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("The red ability boosts Spitfire Handler until end of turn")
    void boostsSelfUntilEndOfTurn() {
        Permanent spitfireHandler = addReadySpitfireHandler(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spitfireHandler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spitfireHandler)).isEqualTo(1);

        spitfireHandler.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, spitfireHandler)).isEqualTo(1);
    }

    private Permanent addReadySpitfireHandler(Player player) {
        return addCreatureReady(player, new SpitfireHandler());
    }
}
