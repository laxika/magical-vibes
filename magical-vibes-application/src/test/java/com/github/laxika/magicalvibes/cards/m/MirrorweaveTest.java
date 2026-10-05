package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.t.ThrunTheLastTroll;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({Mirrorweave.class, GrizzlyBears.class, HillGiant.class, SuntailHawk.class, ThrunTheLastTroll.class})
class MirrorweaveTest extends BaseCardTest {

    private void giveMirrorweave() {
        harness.setHand(player1, List.of(new Mirrorweave()));
        harness.addMana(player1, ManaColor.WHITE, 4);
    }

    @Test
    @DisplayName("Each other creature becomes a copy of the target")
    void otherCreaturesBecomeCopiesOfTarget() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        giveMirrorweave();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(bears.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(bears.getCard().getPower()).isEqualTo(3);
        assertThat(bears.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The target creature itself is unaffected")
    void targetIsUnaffected() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        addCreatureReady(player1, new GrizzlyBears());

        giveMirrorweave();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(target.getCard().getPower()).isEqualTo(3);
        assertThat(target.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Copying replaces the creature's abilities with the target's")
    void copyReplacesAbilities() {
        // Target a vanilla creature; a flyer copying it loses flying.
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent hawk = addCreatureReady(player1, new SuntailHawk());

        giveMirrorweave();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(hawk.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(hawk.getCard().getKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Creatures both players control are affected")
    void affectsBothPlayersCreatures() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        Permanent enemyBears = addCreatureReady(player2, new GrizzlyBears());

        giveMirrorweave();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(enemyBears.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(enemyBears.getCard().getPower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Copies revert to their original card at end of turn")
    void revertsAtEndOfTurn() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        giveMirrorweave();
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(bears.getCard().getName()).isEqualTo("Hill Giant");

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bears.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(bears.getCard().getPower()).isEqualTo(2);
        assertThat(bears.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a legendary creature")
    void cannotTargetLegendaryCreature() {
        Permanent thrun = addCreatureReady(player1, new ThrunTheLastTroll());
        addCreatureReady(player1, new GrizzlyBears());

        giveMirrorweave();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, thrun.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonlegendary creature");
    }

    @Test
    void affectsLegendaryHexproofCreaturesWithoutTargetingThem() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        Permanent thrun = addCreatureReady(player2, new ThrunTheLastTroll());

        giveMirrorweave();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(thrun.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gqs.hasKeyword(gd, thrun, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void retainsOwnCountersWithoutCopyingTargetsCounters() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        giveMirrorweave();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
    }

    @Test
    void doesNotAffectCreaturesEnteringAfterResolution() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        giveMirrorweave();
        harness.castAndResolveInstant(player1, 0, target.getId());

        Permanent bears = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(bears.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }
}
