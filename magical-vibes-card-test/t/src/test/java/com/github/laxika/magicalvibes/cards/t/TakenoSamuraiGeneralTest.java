package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.k.KondaLordOfEiganjo;
import com.github.laxika.magicalvibes.cards.m.MothriderSamurai;
import com.github.laxika.magicalvibes.cards.o.Ovinize;
import com.github.laxika.magicalvibes.cards.s.SenseiGoldenTail;
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

@CardUsed({TakenoSamuraiGeneral.class, MothriderSamurai.class, KondaLordOfEiganjo.class,
        WanderingOnes.class, SenseiGoldenTail.class, Ovinize.class})
class TakenoSamuraiGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Other Samurai get +1/+1 for each point of Bushido")
    void boostsOtherSamuraiByBushidoValue() {
        addCreatureReady(player1, new TakenoSamuraiGeneral());
        Permanent mothrider = addCreatureReady(player1, new MothriderSamurai());
        Permanent konda = addCreatureReady(player1, new KondaLordOfEiganjo());

        assertThat(gqs.getEffectivePower(gd, mothrider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mothrider)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, konda)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, konda)).isEqualTo(8);
    }

    @Test
    @DisplayName("Takeno does not boost itself, non-Samurai, or opposing Samurai")
    void limitsBoostToOtherOwnSamurai() {
        Permanent takeno = addCreatureReady(player1, new TakenoSamuraiGeneral());
        Permanent nonSamurai = addCreatureReady(player1, new WanderingOnes());
        Permanent opponentSamurai = addCreatureReady(player2, new MothriderSamurai());

        assertThat(gqs.getEffectivePower(gd, takeno)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, takeno)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonSamurai)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonSamurai)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentSamurai)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSamurai)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts Bushido granted to a creature that becomes a Samurai")
    void countsGrantedBushido() {
        addCreatureReady(player1, new TakenoSamuraiGeneral());
        Permanent sensei = addCreatureReady(player1, new SenseiGoldenTail());
        Permanent target = addCreatureReady(player1, new WanderingOnes());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sensei), null,
                target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds granted bushido to printed bushido without counting combat bonuses")
    void countsPrintedAndGrantedBushidoTogether() {
        addCreatureReady(player1, new TakenoSamuraiGeneral());
        Permanent sensei = addCreatureReady(player1, new SenseiGoldenTail());
        Permanent target = addCreatureReady(player1, new KondaLordOfEiganjo());
        addCreatureReady(player2, new WanderingOnes());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sensei), null,
                target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(9);

        declareAttackersAndPrepareBlockers(List.of(2));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 2)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(15);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(15);
    }

    @Test
    @DisplayName("The static bonus ends immediately when Takeno leaves the battlefield")
    void stopsBoostingWhenTakenoLeaves() {
        Permanent takeno = addCreatureReady(player1, new TakenoSamuraiGeneral());
        Permanent target = addCreatureReady(player1, new KondaLordOfEiganjo());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(8);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, takeno);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("A Samurai that loses bushido no longer receives Takeno's bonus")
    void doesNotCountRemovedBushido() {
        addCreatureReady(player1, new TakenoSamuraiGeneral());
        Permanent target = addCreatureReady(player1, new KondaLordOfEiganjo());
        harness.setHand(player1, List.of(new Ovinize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Takeno gets +2/+2 when it becomes blocked")
    void getsBushidoBonusWhenBecomesBlocked() {
        Permanent takeno = addCreatureReady(player1, new TakenoSamuraiGeneral());
        addCreatureReady(player2, new WanderingOnes());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, takeno)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, takeno)).isEqualTo(5);
    }

    @Test
    @DisplayName("Becoming blocked by two creatures triggers bushido only once")
    void bushidoTriggersOnceForMultipleBlockers() {
        Permanent takeno = addCreatureReady(player1, new TakenoSamuraiGeneral());
        addCreatureReady(player2, new WanderingOnes());
        addCreatureReady(player2, new WanderingOnes());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, takeno)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, takeno)).isEqualTo(5);
    }

    @Test
    @DisplayName("Takeno gets +2/+2 when it blocks, until end of turn")
    void getsBushidoBonusWhenBlockingUntilEndOfTurn() {
        addCreatureReady(player1, new WanderingOnes());
        Permanent takeno = addCreatureReady(player2, new TakenoSamuraiGeneral());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, takeno)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, takeno)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, takeno)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, takeno)).isEqualTo(3);
    }
}
