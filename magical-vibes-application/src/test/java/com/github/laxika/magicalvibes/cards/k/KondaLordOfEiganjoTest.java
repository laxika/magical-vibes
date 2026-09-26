package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.r.RendFlesh;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KondaLordOfEiganjo.class, WanderingOnes.class, RendFlesh.class})
class KondaLordOfEiganjoTest extends BaseCardTest {

    @Test
    @DisplayName("Konda gets +5/+5 when it becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());
        addCreatureReady(player2, new WanderingOnes());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, konda)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, konda)).isEqualTo(8);
    }

    @Test
    @DisplayName("Konda gets +5/+5 when it blocks")
    void blocksGetsBushidoBonus() {
        addCreatureReady(player1, new WanderingOnes());
        Permanent konda = addCreatureReady(player2, new KondaLordOfEiganjo());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, konda)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, konda)).isEqualTo(8);
    }

    @Test
    @DisplayName("Konda's Bushido bonus wears off at end of turn")
    void bushidoBonusWearsOffAtEndOfTurn() {
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());
        addCreatureReady(player2, new WanderingOnes());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, konda)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, konda)).isEqualTo(8);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, konda)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, konda)).isEqualTo(3);
    }

    @Test
    @DisplayName("Vigilance keeps Konda untapped when it attacks")
    void vigilanceKeepsKondaUntappedWhenItAttacks() {
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());

        declareAttackers(List.of(0));

        assertThat(konda.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Konda's indestructible survives a destroy effect")
    void indestructibleSurvivesDestroyEffect() {
        Permanent konda = addCreatureReady(player2, new KondaLordOfEiganjo());

        harness.setHand(player1, List.of(new RendFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, konda.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Konda, Lord of Eiganjo");
        harness.assertNotInGraveyard(player2, "Konda, Lord of Eiganjo");
    }
}
