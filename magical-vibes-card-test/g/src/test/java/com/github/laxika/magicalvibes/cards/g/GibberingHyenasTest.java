package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.Breathstealer;
import com.github.laxika.magicalvibes.cards.d.DiscordantSpirit;
import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.l.LeadGolem;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GibberingHyenas.class, FemerefScouts.class, Breathstealer.class, DiscordantSpirit.class, LeadGolem.class})
class GibberingHyenasTest extends BaseCardTest {

    @Test
    @DisplayName("Gibbering Hyenas can block a nonblack creature")
    void canBlockNonBlackCreature() {
        Permanent hyenas = addCreatureReady(player2, new GibberingHyenas());

        addCreatureReady(player1, new FemerefScouts());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(hyenas.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gibbering Hyenas cannot block a black creature")
    void cannotBlockBlackCreature() {
        addCreatureReady(player2, new GibberingHyenas());

        addCreatureReady(player1, new Breathstealer());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block");
    }

    @Test
    @DisplayName("Gibbering Hyenas cannot block a multicolored black creature")
    void cannotBlockMulticoloredBlackCreature() {
        addCreatureReady(player2, new GibberingHyenas());

        addCreatureReady(player1, new DiscordantSpirit());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block");
    }

    @Test
    @DisplayName("Gibbering Hyenas can block a colorless artifact creature")
    void canBlockColorlessCreature() {
        Permanent hyenas = addCreatureReady(player2, new GibberingHyenas());
        Permanent attacker = addCreatureReady(player1, new LeadGolem());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(hyenas.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gibbering Hyenas can attack and deal combat damage")
    void canAttackFreely() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new GibberingHyenas());
        declareAttackers(List.of(0));

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
