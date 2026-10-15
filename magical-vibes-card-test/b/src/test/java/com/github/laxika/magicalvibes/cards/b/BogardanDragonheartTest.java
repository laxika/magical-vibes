package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BogardanDragonheart.class, MotherBear.class, AmoeboidChangeling.class})
class BogardanDragonheartTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature turns it into a 4/4 Dragon with flying and haste")
    void sacrificeAnotherCreatureTransformsDragonheart() {
        Permanent dragonheart = addCreatureReady(player1, new BogardanDragonheart());
        harness.addToBattlefield(player1, new MotherBear());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mother Bear");
        assertThat(gqs.effectiveCreatureSubtypes(gd, dragonheart))
                .containsExactly(CardSubtype.DRAGON);
        assertThat(gqs.getEffectivePower(gd, dragonheart)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dragonheart)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, dragonheart, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, dragonheart, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The Dragon transformation wears off at end of turn")
    void transformationWearsOffAtEndOfTurn() {
        Permanent dragonheart = addCreatureReady(player1, new BogardanDragonheart());
        harness.addToBattlefield(player1, new MotherBear());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.effectiveCreatureSubtypes(gd, dragonheart))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SHAMAN);
        assertThat(gqs.getEffectivePower(gd, dragonheart)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dragonheart)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, dragonheart, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, dragonheart, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without another creature to sacrifice")
    void cannotActivateWithoutAnotherCreature() {
        addCreatureReady(player1, new BogardanDragonheart());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid before the transformation resolves")
    void sacrificeIsAnActivationCost() {
        Permanent dragonheart = addCreatureReady(player1, new BogardanDragonheart());
        harness.addToBattlefield(player1, new MotherBear());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Mother Bear");
        harness.assertNotOnBattlefield(player1, "Mother Bear");
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, dragonheart)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, dragonheart))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SHAMAN);

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, dragonheart)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void activationDoesNotRequireTappingOrHaste() {
        Permanent dragonheart = harness.addToBattlefieldAndReturn(player1, new BogardanDragonheart());
        dragonheart.setSummoningSick(true);
        dragonheart.tap();
        harness.addToBattlefield(player1, new MotherBear());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dragonheart.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, dragonheart)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, dragonheart, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        addCreatureReady(player1, new BogardanDragonheart());
        harness.addToBattlefield(player2, new MotherBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Mother Bear");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters remain additive and repeated activation retains the ability")
    void repeatedActivationDoesNotOverwriteCountersOrRemoveAbility() {
        Permanent dragonheart = addCreatureReady(player1, new BogardanDragonheart());
        dragonheart.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player1, new MotherBear());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, dragonheart)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dragonheart)).isEqualTo(5);

        harness.addToBattlefield(player1, new MotherBear());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dragonheart)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dragonheart)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, dragonheart)).containsExactly(CardSubtype.DRAGON);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, dragonheart)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dragonheart)).isEqualTo(3);
    }

    @Test
    @DisplayName("A later effect removing all creature types overrides the Dragon transformation")
    void laterTypeRemovalOverridesDragonType() {
        Permanent dragonheart = addCreatureReady(player1, new BogardanDragonheart());
        harness.addToBattlefield(player1, new MotherBear());
        addCreatureReady(player2, new AmoeboidChangeling());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.effectiveCreatureSubtypes(gd, dragonheart)).containsExactly(CardSubtype.DRAGON);

        harness.activateAbility(player2, 0, 1, null, dragonheart.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, dragonheart)).isEmpty();
        assertThat(gqs.getEffectivePower(gd, dragonheart)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dragonheart)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, dragonheart, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, dragonheart, Keyword.HASTE)).isTrue();
    }
}
