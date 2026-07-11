package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MirrorEntityTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {X} puts the ability on the stack with the paid X")
    void activatingPutsAbilityOnStackWithX() {
        addCreatureReady(player1, new MirrorEntity());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 4, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getXValue()).isEqualTo(4);
    }

    @Test
    @DisplayName("Resolving with X=4 sets base power/toughness of your creatures (and Mirror Entity) to 4/4")
    void resolvingSetsOwnCreaturesToXX() {
        Permanent entity = addCreatureReady(player1, new MirrorEntity());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 4, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, entity)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, entity)).isEqualTo(4);
    }

    @Test
    @DisplayName("Base P/X is set: a +1/+1 counter still applies on top of the new base")
    void modifiersApplyOnTopOfNewBase() {
        addCreatureReady(player1, new MirrorEntity());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 4, null);
        harness.passBothPriorities();

        // Base 4/4 from Mirror Entity + 1/1 counter = 5/5
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Your creatures gain all creature types until end of turn")
    void ownCreaturesGainAllCreatureTypes() {
        addCreatureReady(player1, new MirrorEntity());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.ELF)).isFalse();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        // Now a changeling — has every creature type
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.ELF)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.GOBLIN)).isTrue();
    }

    @Test
    @DisplayName("Only affects creatures you control, not opponents'")
    void doesNotAffectOpponentCreatures() {
        addCreatureReady(player1, new MirrorEntity());
        Permanent oppBears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 4, null);
        harness.passBothPriorities();

        // Opponent's Grizzly Bears stays a vanilla 2/2 with no granted types
        assertThat(gqs.getEffectivePower(gd, oppBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oppBears)).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(oppBears, CardSubtype.ELF)).isFalse();
    }

    @Test
    @DisplayName("Effect wears off at end of turn — base P/T and creature types revert")
    void wearsOffAtEndOfTurn() {
        addCreatureReady(player1, new MirrorEntity());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 4, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.ELF)).isFalse();
    }
}
