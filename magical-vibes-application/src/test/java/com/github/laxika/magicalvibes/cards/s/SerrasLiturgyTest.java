package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SerrasLiturgyTest extends BaseCardTest {

    @Test
    void upkeepMayAddVerseCounter() {
        Permanent liturgy = addLiturgy(0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(liturgy.getCounterCount(CounterType.VERSE)).isEqualTo(1);
    }

    @Test
    void destroysUpToVerseCounterArtifactsAndEnchantments() {
        addLiturgy(2);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ZuranOrb());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AuraOfSilence());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(artifact.getId(), enchantment.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Zuran Orb");
        harness.assertNotOnBattlefield(player2, "Aura of Silence");
        harness.assertNotOnBattlefield(player1, "Serra's Liturgy");
    }

    @Test
    void cannotChooseMoreTargetsThanVerseCounters() {
        addLiturgy(1);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AuraOfSilence());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AuraOfSilence());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between 0 and 1 targets");
    }

    @Test
    void cannotTargetNonArtifactOrNonEnchantmentPermanent() {
        addLiturgy(1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Targets must be artifacts and/or enchantments");
    }

    private Permanent addLiturgy(int verseCounters) {
        harness.addToBattlefield(player1, new SerrasLiturgy());
        Permanent liturgy = findPermanent(player1, "Serra's Liturgy");
        liturgy.setCounterCount(CounterType.VERSE, verseCounters);
        harness.addMana(player1, ManaColor.WHITE, 1);
        return liturgy;
    }
}
