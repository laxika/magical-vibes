package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.Breathstealer;
import com.github.laxika.magicalvibes.cards.d.DiscordantSpirit;
import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GibberingHyenas.class, FemerefScouts.class, Breathstealer.class, DiscordantSpirit.class})
class GibberingHyenasTest extends BaseCardTest {

    @Test
    @DisplayName("Gibbering Hyenas can block a nonblack creature")
    void canBlockNonBlackCreature() {
        Permanent hyenas = addCreatureReady(player2, new GibberingHyenas());

        Permanent attacker = addCreatureReady(player1, new FemerefScouts());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(hyenas.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gibbering Hyenas cannot block a black creature")
    void cannotBlockBlackCreature() {
        Permanent hyenas = addCreatureReady(player2, new GibberingHyenas());

        Permanent attacker = addCreatureReady(player1, new Breathstealer());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block");
    }

    @Test
    @DisplayName("Gibbering Hyenas cannot block a multicolored black creature")
    void cannotBlockMulticoloredBlackCreature() {
        Permanent hyenas = addCreatureReady(player2, new GibberingHyenas());

        Permanent attacker = addCreatureReady(player1, new DiscordantSpirit());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block");
    }

    @Test
    @DisplayName("Gibbering Hyenas can attack and deal combat damage")
    void canAttackFreely() {
        harness.setLife(player2, 20);

        Permanent hyenas = addCreatureReady(player1, new GibberingHyenas());
        hyenas.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
