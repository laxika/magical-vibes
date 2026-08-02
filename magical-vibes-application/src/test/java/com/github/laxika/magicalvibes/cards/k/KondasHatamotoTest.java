package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KondasHatamotoTest extends BaseCardTest {

    @Test
    @DisplayName("Bushido gives Konda's Hatamoto +1/+1 when it becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        Permanent hatamoto = addCreatureReady(player1, new KondasHatamoto());
        hatamoto.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hatamoto)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hatamoto)).isEqualTo(3);
    }

    @Test
    @DisplayName("Konda's Hatamoto gets +1/+2 and vigilance with a legendary Samurai")
    void gainsBonusWithLegendarySamurai() {
        Permanent hatamoto = addCreatureReady(player1, new KondasHatamoto());
        addCreatureReady(player1, new KondaLordOfEiganjo());

        assertThat(gqs.getEffectivePower(gd, hatamoto)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hatamoto)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, hatamoto, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's legendary Samurai does not help Konda's Hatamoto")
    void opponentLegendarySamuraiDoesNotGrantBonus() {
        Permanent hatamoto = addCreatureReady(player1, new KondasHatamoto());
        addCreatureReady(player2, new KondaLordOfEiganjo());

        assertThat(gqs.getEffectivePower(gd, hatamoto)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hatamoto)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hatamoto, Keyword.VIGILANCE)).isFalse();
    }
}
