package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({SeedsOfStrength.class, BorosRecruit.class, Mountain.class, LastGasp.class})
class SeedsOfStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Gives three target creatures +1/+1 each")
    void boostsThreeTargetCreatures() {
        Permanent first = addCreature();
        Permanent second = addCreature();
        Permanent third = addCreature();

        castSeedsOfStrength(List.of(first.getId(), second.getId(), third.getId()));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, third)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void boostsOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());

        castSeedsOfStrength(List.of(creature.getId(), creature.getId(), creature.getId()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Allows the same creature to be targeted three times")
    void stacksAllThreeBoostsOnOneTarget() {
        Permanent creature = addCreature();

        castSeedsOfStrength(List.of(creature.getId(), creature.getId(), creature.getId()));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Requires exactly three creature targets")
    void requiresExactlyThreeTargets() {
        Permanent creature = addCreature();
        harness.setHand(player1, List.of(new SeedsOfStrength()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent first = addCreature();
        Permanent second = addCreature();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new SeedsOfStrength()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(first.getId(), second.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The boosts expire at end of turn")
    void boostsExpireAtEndOfTurn() {
        Permanent creature = addCreature();
        castSeedsOfStrength(List.of(creature.getId(), creature.getId(), creature.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can split the boosts between two creatures")
    void splitsBoostsBetweenTwoCreatures() {
        Permanent first = addCreature();
        Permanent second = addCreature();

        castSeedsOfStrength(List.of(first.getId(), second.getId(), first.getId()));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("Still boosts the remaining targets when the first target dies in response")
    void resolvesWithFirstTargetRemoved() {
        Permanent first = addCreature();
        Permanent second = addCreature();
        Permanent third = addCreature();
        harness.setHand(player1, List.of(new SeedsOfStrength()));
        addMana();
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId(), third.getId()));

        killInResponse(first);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, third)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Seeds of Strength");
    }

    @Test
    @DisplayName("Keeps both boosts on the surviving repeated target")
    void resolvesWithRepeatedTargetAndMiddleTargetRemoved() {
        Permanent survivor = addCreature();
        Permanent removed = addCreature();
        harness.setHand(player1, List.of(new SeedsOfStrength()));
        addMana();
        harness.castInstant(player1, 0, List.of(survivor.getId(), removed.getId(), survivor.getId()));

        killInResponse(removed);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Seeds of Strength");
    }

    @Test
    @DisplayName("Does not resolve when all three target occurrences become illegal")
    void doesNotResolveWithAllTargetsRemoved() {
        Permanent removed = addCreature();
        Permanent other = addCreature();
        harness.setHand(player1, List.of(new SeedsOfStrength()));
        addMana();
        harness.castInstant(player1, 0, List.of(removed.getId(), removed.getId(), removed.getId()));

        killInResponse(removed);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Seeds of Strength");
    }

    private void killInResponse(Permanent creature) {
        harness.setHand(player2, List.of(new LastGasp()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player1, "Boros Recruit");
    }

    private Permanent addCreature() {
        return harness.addToBattlefieldAndReturn(player1, new BorosRecruit());
    }

    private void castSeedsOfStrength(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new SeedsOfStrength()));
        addMana();
        harness.castAndResolveInstant(player1, 0, targetIds);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
