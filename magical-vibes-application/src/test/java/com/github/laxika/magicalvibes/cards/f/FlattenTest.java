package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AncientCarp;
import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.cards.k.KolaghanSkirmisher;
import com.github.laxika.magicalvibes.cards.s.SegmentedKrotiq;
import com.github.laxika.magicalvibes.cards.s.SpidersilkNet;
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

@CardUsed({Flatten.class, SegmentedKrotiq.class, SpidersilkNet.class, KolaghanSkirmisher.class,
        AncientCarp.class, ColossodonYearling.class})
class FlattenTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives target creature -4/-4 until end of turn")
    void resolvingGivesMinusFourMinusFour() {
        Permanent krotiq = harness.addToBattlefieldAndReturn(player2, new SegmentedKrotiq());
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, krotiq.getId());

        assertThat(gqs.getEffectivePower(gd, krotiq)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, krotiq)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature with toughness 2 dies to the -4 toughness")
    void twoToughnessCreatureDies() {
        Permanent skirmisher = harness.addToBattlefieldAndReturn(player2, new KolaghanSkirmisher());
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, skirmisher.getId());

        harness.assertNotOnBattlefield(player2, "Kolaghan Skirmisher");
        harness.assertInGraveyard(player2, "Kolaghan Skirmisher");
    }

    @Test
    @DisplayName("The debuff wears off at cleanup")
    void debuffWearsOff() {
        Permanent krotiq = harness.addToBattlefieldAndReturn(player1, new SegmentedKrotiq());
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, krotiq.getId());

        assertThat(gqs.getEffectivePower(gd, krotiq)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, krotiq)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, krotiq)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, krotiq)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new KolaghanSkirmisher());
        Permanent net = harness.addToBattlefieldAndReturn(player2, new SpidersilkNet());
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, net.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Exactly four toughness becomes zero and the creature dies")
    void fourToughnessCreatureDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ColossodonYearling());
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Colossodon Yearling");
        harness.assertInGraveyard(player2, "Colossodon Yearling");
    }

    @Test
    @DisplayName("Negative power does not kill a creature with positive toughness")
    void negativePowerCreatureSurvives() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientCarp());
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Ancient Carp");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated casts accumulate and kill a creature that survived the first")
    void repeatedCastsAccumulate() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SegmentedKrotiq());
        harness.setHand(player1, List.of(new Flatten(), new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.assertOnBattlefield(player2, "Segmented Krotiq");
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Segmented Krotiq");
        harness.assertInGraveyard(player2, "Segmented Krotiq");
    }

    @Test
    @DisplayName("A target killed in response leaves the earlier spell with no legal target")
    void targetKilledInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KolaghanSkirmisher());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new AncientCarp());
        harness.setHand(player1, List.of(new Flatten(), new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Kolaghan Skirmisher");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Flatten).hasSize(2);
        harness.assertInGraveyard(player2, "Kolaghan Skirmisher");
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(5);
    }
}
