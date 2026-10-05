package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BatteringWurm;
import com.github.laxika.magicalvibes.cards.e.Electrolyze;
import com.github.laxika.magicalvibes.cards.g.GiantSolifuge;
import com.github.laxika.magicalvibes.cards.r.Repeal;
import com.github.laxika.magicalvibes.cards.s.SeedsOfStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InkTreaderNephilim.class, BatteringWurm.class, Electrolyze.class, GiantSolifuge.class,
        Repeal.class, SeedsOfStrength.class})
class InkTreaderNephilimTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a spell for every other legal creature and controls the copies")
    void copiesForEveryOtherCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new InkTreaderNephilim());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BatteringWurm());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new BatteringWurm());
        harness.addToBattlefield(player2, new GiantSolifuge());
        harness.setHand(player2, List.of(new Electrolyze()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, Map.of(source.getId(), 2));
        harness.passBothPriorities();

        List<StackEntry> copies = gd.stack.stream().filter(StackEntry::isCopy).toList();
        assertThat(copies).hasSize(2);
        assertThat(copies).extracting(StackEntry::getTargetId)
                .containsExactlyInAnyOrder(ownCreature.getId(), opposingCreature.getId());
        assertThat(copies).allMatch(copy -> copy.getControllerId().equals(player1.getId()));
    }

    @Test
    @DisplayName("Does not trigger for a spell targeting another permanent")
    void doesNotTriggerForAnotherPermanent() {
        harness.addToBattlefield(player1, new InkTreaderNephilim());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new BatteringWurm());
        harness.setHand(player1, List.of(new Electrolyze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, Map.of(otherCreature.getId(), 2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Electrolyze");
    }

    @Test
    @DisplayName("Does not trigger for a spell targeting a player")
    void doesNotTriggerForPlayerTarget() {
        harness.addToBattlefield(player1, new InkTreaderNephilim());
        harness.setHand(player1, List.of(new Electrolyze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, Map.of(player2.getId(), 2));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Electrolyze");
    }

    @Test
    @DisplayName("Does not trigger when the spell also targets another object")
    void doesNotTriggerForAdditionalTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new InkTreaderNephilim());
        harness.setHand(player1, List.of(new Electrolyze()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, Map.of(source.getId(), 1, player2.getId(), 1));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Electrolyze");
    }

    @Test
    @DisplayName("Copies a single-target spell with its target changed to each legal creature")
    void copiesSingleTargetDividedDamageSpellToLegalCreatures() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new InkTreaderNephilim());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BatteringWurm());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new BatteringWurm());
        harness.setLibrary(player1, List.of(new BatteringWurm(), new BatteringWurm()));
        harness.setLibrary(player2, List.of(new BatteringWurm()));
        harness.setHand(player2, List.of(new Electrolyze()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, Map.of(source.getId(), 2));
        resolveAllTriggers();

        assertThat(source.getMarkedDamage()).isEqualTo(2);
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Uses the spell's chosen X when determining which creatures it could target")
    void copiesRepealForCreaturesWithTheChosenManaValue() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new InkTreaderNephilim());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new InkTreaderNephilim());
        harness.addToBattlefield(player2, new BatteringWurm());
        harness.setHand(player1, List.of(new Repeal()));
        harness.setLibrary(player1, List.of(new BatteringWurm(), new BatteringWurm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, 4, source.getId());
        harness.passBothPriorities();

        List<StackEntry> copies = gd.stack.stream().filter(StackEntry::isCopy).toList();
        assertThat(copies).hasSize(1);
        assertThat(copies.getFirst().getTargetId()).isEqualTo(other.getId());
        assertThat(copies.getFirst().getXValue()).isEqualTo(4);

        resolveAllTriggers();

        harness.assertInHand(player1, "Ink-Treader Nephilim");
        harness.assertInHand(player2, "Ink-Treader Nephilim");
        harness.assertOnBattlefield(player2, "Battering Wurm");
    }

    @Test
    @DisplayName("Changes every target of a spell that targets the Nephilim multiple times")
    void retargetsEveryInstanceOfTheSameTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new InkTreaderNephilim());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new BatteringWurm());
        harness.setHand(player1, List.of(new SeedsOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, List.of(source.getId(), source.getId(), source.getId()));
        harness.passBothPriorities();

        List<StackEntry> copies = gd.stack.stream().filter(StackEntry::isCopy).toList();
        assertThat(copies).hasSize(1);
        assertThat(copies.getFirst().getDeclaredTargetIds())
                .containsExactly(other.getId(), other.getId(), other.getId());

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(6);
    }
}
