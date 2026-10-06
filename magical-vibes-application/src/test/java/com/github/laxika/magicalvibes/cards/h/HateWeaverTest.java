package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Firescreamer;
import com.github.laxika.magicalvibes.cards.k.KavuAggressor;
import com.github.laxika.magicalvibes.cards.s.SkyWeaver;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HateWeaver.class, SkyWeaver.class, KavuAggressor.class, Firescreamer.class, Swamp.class})
class HateWeaverTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a blue creature by +1/+0 until end of turn")
    void boostsBlueCreature() {
        addCreatureReady(player1, new HateWeaver());
        Permanent target = addCreatureReady(player2, new SkyWeaver());
        int basePower = gqs.getEffectivePower(gd, target);
        int baseToughness = gqs.getEffectiveToughness(gd, target);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Boosts a red creature controlled by its controller")
    void boostsRedCreature() {
        addCreatureReady(player1, new HateWeaver());
        Permanent target = addCreatureReady(player1, new KavuAggressor());
        int basePower = gqs.getEffectivePower(gd, target);
        int baseToughness = gqs.getEffectiveToughness(gd, target);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Costs two generic mana and does not tap Hate Weaver")
    void costsTwoGenericManaWithoutTappingSource() {
        Permanent hateWeaver = addCreatureReady(player1, new HateWeaver());
        Permanent target = addCreatureReady(player2, new SkyWeaver());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(hateWeaver.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple activations give cumulative power bonuses")
    void activationsStack() {
        addCreatureReady(player1, new HateWeaver());
        Permanent target = addCreatureReady(player2, new KavuAggressor());
        int basePower = gqs.getEffectivePower(gd, target);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 2);
    }

    @Test
    @DisplayName("Power bonus wears off at end of turn")
    void bonusExpiresAtEndOfTurn() {
        addCreatureReady(player1, new HateWeaver());
        Permanent target = addCreatureReady(player2, new SkyWeaver());
        int basePower = gqs.getEffectivePower(gd, target);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Cannot target a creature that is not blue or red")
    void rejectsCreatureWithNoMatchingColor() {
        addCreatureReady(player1, new HateWeaver());
        Permanent target = addCreatureReady(player2, new Firescreamer());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a blue or red creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNonCreaturePermanent() {
        addCreatureReady(player1, new HateWeaver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a blue or red creature");
    }

    @Test
    @DisplayName("Cannot activate without two generic mana")
    void rejectsInsufficientMana() {
        addCreatureReady(player1, new HateWeaver());
        Permanent target = addCreatureReady(player2, new SkyWeaver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick using colored mana")
    void activatesWhileTappedAndSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HateWeaver());
        source.setSummoningSick(true);
        source.tap();
        Permanent target = addCreatureReady(player2, new SkyWeaver());
        int basePower = gqs.getEffectivePower(gd, target);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 1);
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Ability resolves after Hate Weaver leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        Permanent source = addCreatureReady(player1, new HateWeaver());
        Permanent target = addCreatureReady(player2, new SkyWeaver());
        int basePower = gqs.getEffectivePower(gd, target);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(basePower + 1);
        harness.assertInGraveyard(player1, "Hate Weaver");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability does not boost a target that leaves before resolution")
    void doesNotBoostRemovedTarget() {
        addCreatureReady(player1, new HateWeaver());
        Permanent target = addCreatureReady(player2, new SkyWeaver());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        harness.assertInGraveyard(player2, "Sky Weaver");
        assertThat(gd.stack).isEmpty();
    }
}
