package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZhalfirinKnight.class, FemerefScouts.class})
class ZhalfirinKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Zhalfirin Knight puts it onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new ZhalfirinKnight(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zhalfirin Knight");
    }

    @Test
    @DisplayName("Resolving the ability grants first strike until end of turn")
    void resolvingAbilityGrantsFirstStrike() {
        Permanent knight = addCreatureReady(player1, new ZhalfirinKnight());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike wears off at end of turn")
    void firstStrikeWearsOff() {
        Permanent knight = addCreatureReady(player1, new ZhalfirinKnight());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, knight, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Ability requires {W}{W} — one white mana is not enough")
    void requiresTwoWhiteMana() {
        addCreatureReady(player1, new ZhalfirinKnight());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability can be activated while tapped and does not tap the knight")
    void canActivateWhileTapped() {
        Permanent knight = addCreatureReady(player1, new ZhalfirinKnight());
        knight.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(knight.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Flanking gives a non-flanking blocker -1/-1 until end of turn")
    void flankingShrinksNonFlankingBlocker() {
        Permanent knight = addCreatureReady(player1, new ZhalfirinKnight());
        knight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FemerefScouts());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Flanking does not affect a blocker that also has flanking")
    void flankingDoesNotShrinkFlankingBlocker() {
        Permanent knight = addCreatureReady(player1, new ZhalfirinKnight());
        knight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ZhalfirinKnight());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flanking affects each blocker independently and wears off at end of turn")
    void flankingAffectsEachBlockerUntilEndOfTurn() {
        Permanent knight = addCreatureReady(player1, new ZhalfirinKnight());
        knight.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new FemerefScouts());
        Permanent secondBlocker = addCreatureReady(player2, new FemerefScouts());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, firstBlocker)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, firstBlocker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondBlocker)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, secondBlocker)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, firstBlocker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, firstBlocker)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondBlocker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, secondBlocker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Flanking does not trigger when the knight blocks")
    void flankingDoesNotTriggerWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new FemerefScouts());
        attacker.setAttacking(true);
        addCreatureReady(player2, new ZhalfirinKnight());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Activated first strike kills an opposing knight before it deals combat damage")
    void activatedFirstStrikeWinsCombat() {
        Permanent knight = addCreatureReady(player1, new ZhalfirinKnight());
        knight.setAttacking(true);
        addCreatureReady(player2, new ZhalfirinKnight());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertOnBattlefield(player1, "Zhalfirin Knight");
        harness.assertInGraveyard(player2, "Zhalfirin Knight");
        assertThat(knight.getMarkedDamage()).isZero();
    }
}
