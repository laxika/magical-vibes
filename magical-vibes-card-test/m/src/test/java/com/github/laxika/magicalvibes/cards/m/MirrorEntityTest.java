package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.k.KithkinHealer;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrorEntity.class, KithkinHealer.class, Lignify.class})
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
        Permanent healer = addCreatureReady(player1, new KithkinHealer());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 4, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, healer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, healer)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, entity)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, entity)).isEqualTo(4);
    }

    @Test
    @DisplayName("Base P/X is set: a +1/+1 counter still applies on top of the new base")
    void modifiersApplyOnTopOfNewBase() {
        addCreatureReady(player1, new MirrorEntity());
        Permanent healer = addCreatureReady(player1, new KithkinHealer());
        healer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 4, null);
        harness.passBothPriorities();

        // Base 4/4 from Mirror Entity + 1/1 counter = 5/5
        assertThat(gqs.getEffectivePower(gd, healer)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, healer)).isEqualTo(5);
    }

    @Test
    @DisplayName("Your creatures gain all creature types until end of turn")
    void ownCreaturesGainAllCreatureTypes() {
        addCreatureReady(player1, new MirrorEntity());
        Permanent healer = addCreatureReady(player1, new KithkinHealer());
        assertThat(GameQueryService.permanentHasSubtype(healer, CardSubtype.ELF)).isFalse();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        // Now a changeling — has every creature type
        assertThat(GameQueryService.permanentHasSubtype(healer, CardSubtype.ELF)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(healer, CardSubtype.GOBLIN)).isTrue();
    }

    @Test
    @DisplayName("Only affects creatures you control, not opponents'")
    void doesNotAffectOpponentCreatures() {
        addCreatureReady(player1, new MirrorEntity());
        Permanent opponentHealer = addCreatureReady(player2, new KithkinHealer());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 4, null);
        harness.passBothPriorities();

        // Opponent's Kithkin Healer stays a 2/2 with no granted types
        assertThat(gqs.getEffectivePower(gd, opponentHealer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentHealer)).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(opponentHealer, CardSubtype.ELF)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after resolution are not affected")
    void doesNotAffectCreaturesEnteringAfterResolution() {
        addCreatureReady(player1, new MirrorEntity());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 4, null);
        harness.passBothPriorities();

        Permanent laterHealer = addCreatureReady(player1, new KithkinHealer());

        assertThat(gqs.getEffectivePower(gd, laterHealer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterHealer)).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(laterHealer, CardSubtype.ELF)).isFalse();
    }

    @Test
    @DisplayName("Effect wears off at end of turn — base P/T and creature types revert")
    void wearsOffAtEndOfTurn() {
        addCreatureReady(player1, new MirrorEntity());
        Permanent healer = addCreatureReady(player1, new KithkinHealer());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 4, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, healer)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, healer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, healer)).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(healer, CardSubtype.ELF)).isFalse();
    }

    @Test
    void zeroActivationKillsCreaturesWithoutToughnessBonuses() {
        addCreatureReady(player1, new MirrorEntity());
        Permanent healer = addCreatureReady(player1, new KithkinHealer());
        healer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mirror Entity");
        harness.assertOnBattlefield(player1, "Kithkin Healer");
        assertThat(gqs.getEffectivePower(gd, healer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, healer)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, healer, CardSubtype.ELF)).isTrue();
    }

    @Test
    void lastResolvingActivationDeterminesBasePowerAndToughness() {
        Permanent entity = addCreatureReady(player1, new MirrorEntity());
        Permanent healer = addCreatureReady(player1, new KithkinHealer());
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.activateAbility(player1, 0, 2, null);
        harness.activateAbility(player1, 0, 5, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, healer)).isEqualTo(5);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, healer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, healer)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, entity)).isEqualTo(2);
    }

    @Test
    void resolvingAbilityGivesLignifiedSourceAllCreatureTypes() {
        Permanent entity = addCreatureReady(player1, new MirrorEntity());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 0, 4, null);
        Permanent lignify = harness.addToBattlefieldAndReturn(player2, new Lignify());
        lignify.setAttachedTo(entity.getId());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, entity)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, entity)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, entity, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, entity, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasKeyword(gd, entity, Keyword.CHANGELING)).isFalse();
    }

    @Test
    void gainingAllCreatureTypesDoesNotGrantChangelingAbility() {
        addCreatureReady(player1, new MirrorEntity());
        Permanent healer = addCreatureReady(player1, new KithkinHealer());
        Permanent lignify = harness.addToBattlefieldAndReturn(player2, new Lignify());
        lignify.setAttachedTo(healer.getId());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 4, null);
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, healer, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, healer, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasKeyword(gd, healer, Keyword.CHANGELING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, healer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, healer)).isEqualTo(4);
    }
}
