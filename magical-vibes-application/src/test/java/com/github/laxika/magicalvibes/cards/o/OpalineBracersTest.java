package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OpalineBracers.class, Arachnoid.class})
class OpalineBracersTest extends BaseCardTest {

    @Test
    void sunburstPutsOneChargeCounterForEachColorSpent() {
        harness.setHand(player1, List.of(new OpalineBracers()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent bracers = findPermanent(player1, "Opaline Bracers");
        assertThat(bracers.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void sunburstCountsEachColorOnlyOnce() {
        harness.setHand(player1, List.of(new OpalineBracers()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent bracers = findPermanent(player1, "Opaline Bracers");
        assertThat(bracers.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void equippedCreatureGetsPlusOnePlusOneForEachChargeCounter() {
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        Permanent bracers = harness.addToBattlefieldAndReturn(player1, new OpalineBracers());
        bracers.setCounterCount(CounterType.CHARGE, 3);
        bracers.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(9);
    }

    @Test
    void unattachedBracersDoNotBoostCreature() {
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        Permanent bracers = harness.addToBattlefieldAndReturn(player1, new OpalineBracers());
        bracers.setCounterCount(CounterType.CHARGE, 3);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    void equipAttachesAndUsesCurrentChargeCounterCount() {
        Permanent bracers = harness.addToBattlefieldAndReturn(player1, new OpalineBracers());
        bracers.setCounterCount(CounterType.CHARGE, 2);
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(bracers.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
    }

    @Test
    void equipAbilityRequiresCreatureYouControl() {
        harness.addToBattlefieldAndReturn(player1, new OpalineBracers());
        Permanent opponentCreature = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void equipAbilityRequiresSorcerySpeed() {
        harness.addToBattlefieldAndReturn(player1, new OpalineBracers());
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }
}
