package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KondasHatamoto.class, KondaLordOfEiganjo.class, WanderingOnes.class,
        IsamaruHoundOfKonda.class})
class KondasHatamotoTest extends BaseCardTest {

    @Test
    @DisplayName("Bushido gives Konda's Hatamoto +1/+1 when it becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        Permanent hatamoto = addCreatureReady(player1, new KondasHatamoto());
        addCreatureReady(player2, new WanderingOnes());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hatamoto)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hatamoto)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bushido gives Konda's Hatamoto +1/+1 when it blocks")
    void blocksGetsBushidoBonus() {
        addCreatureReady(player1, new WanderingOnes());
        Permanent hatamoto = addCreatureReady(player2, new KondasHatamoto());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
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
    @DisplayName("Vigilance keeps Konda's Hatamoto untapped when it attacks")
    void vigilanceKeepsHatamotoUntappedWhenItAttacks() {
        Permanent hatamoto = addCreatureReady(player1, new KondasHatamoto());
        addCreatureReady(player1, new KondaLordOfEiganjo());

        declareAttackers(List.of(0));

        assertThat(hatamoto.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A legendary non-Samurai does not help Konda's Hatamoto")
    void legendaryNonSamuraiDoesNotGrantBonus() {
        Permanent hatamoto = addCreatureReady(player1, new KondasHatamoto());
        addCreatureReady(player1, new IsamaruHoundOfKonda());

        assertThat(gqs.getEffectivePower(gd, hatamoto)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hatamoto)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hatamoto, Keyword.VIGILANCE)).isFalse();
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

    @Test
    @DisplayName("Konda's Hatamoto's Bushido bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent hatamoto = addCreatureReady(player1, new KondasHatamoto());
        addCreatureReady(player2, new WanderingOnes());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hatamoto)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hatamoto)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hatamoto)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hatamoto)).isEqualTo(2);
    }
}
