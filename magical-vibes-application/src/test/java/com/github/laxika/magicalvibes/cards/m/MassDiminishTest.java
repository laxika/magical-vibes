package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({MassDiminish.class, FountainOfYouth.class, GrizzlyBears.class, SerraAngel.class})
class MassDiminishTest extends BaseCardTest {

    @Test
    @DisplayName("Sets all creatures controlled by the target player to base 1/1")
    void setsTargetPlayersCreaturesToOneOne() {
        Permanent targetBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent targetAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAndResolve(player2.getId());

        assertThat(gqs.getEffectivePower(gd, targetBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, targetBear)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, targetAngel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, targetAngel)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("The mass shrink lasts through cleanup and ends on the caster's next turn")
    void lastsUntilCastersNextTurn() {
        Permanent targetBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(player2.getId());
        assertThat(gqs.getEffectivePower(gd, targetBear)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, targetBear)).isEqualTo(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UNTAP);

        assertThat(gqs.getEffectivePower(gd, targetBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, targetBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flashback applies the same effect and exiles Mass Diminish")
    void flashbackAppliesEffectAndExilesSpell() {
        Permanent targetBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new MassDiminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        assertThat(gqs.getEffectivePower(gd, targetBear)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Mass Diminish");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Mass Diminish"));
    }

    @Test
    @DisplayName("Mass Diminish only accepts a player target")
    void rejectsPermanentTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MassDiminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only target players");
    }

    @Test
    @DisplayName("Can target the caster without affecting the opponent's creatures")
    void canTargetCaster() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(player1.getId());

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution are not diminished")
    void doesNotAffectLaterCreatures() {
        Permanent originalBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(player2.getId());
        Permanent laterBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, originalBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, originalBear)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, laterBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counters still modify the new base stats and abilities remain")
    void preservesCountersAndAbilities() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        angel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castAndResolve(player2.getId());

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("A player without creatures remains a legal target")
    void resolvesWithNoCreatures() {
        castAndResolve(player2.getId());

        harness.assertInGraveyard(player1, "Mass Diminish");
        Permanent laterBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, laterBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterBear)).isEqualTo(2);
    }

    private void castAndResolve(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new MassDiminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }
}
