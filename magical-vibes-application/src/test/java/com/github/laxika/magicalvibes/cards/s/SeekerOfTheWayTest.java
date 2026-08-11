package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeekerOfTheWayTest extends BaseCardTest {

    private Permanent addSeeker() {
        harness.addToBattlefield(player1, new SeekerOfTheWay());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Casting a noncreature spell gives +1/+1 and lifelink until end of turn")
    void noncreatureSpellPumpsAndGrantsLifelink() {
        Permanent seeker = addSeeker();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isEqualTo(2);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, seeker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, seeker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, seeker, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger either ability")
    void creatureSpellDoesNotTrigger() {
        Permanent seeker = addSeeker();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, seeker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, seeker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, seeker, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The prowess boost and temporary lifelink wear off at end of turn")
    void temporaryAbilitiesWearOffAtEndOfTurn() {
        Permanent seeker = addSeeker();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, seeker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, seeker, Keyword.LIFELINK)).isTrue();

        endTurn();

        assertThat(gqs.getEffectivePower(gd, seeker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, seeker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, seeker, Keyword.LIFELINK)).isFalse();
    }
}
