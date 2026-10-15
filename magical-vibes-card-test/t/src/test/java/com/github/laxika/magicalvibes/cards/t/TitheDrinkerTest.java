package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TitheDrinker.class, KraulWarrior.class})
class TitheDrinkerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell offers Extort and paying with black drains the opponent")
    void payingExtortWithBlackDrainsOpponent() {
        harness.addToBattlefield(player1, new TitheDrinker());
        harness.setHand(player1, List.of(new KraulWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Declining Extort does nothing")
    void decliningExtortDoesNothing() {
        harness.addToBattlefield(player1, new TitheDrinker());
        harness.setHand(player1, List.of(new KraulWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Tithe Drinker does not trigger for an opponent's spell")
    void opponentSpellDoesNotTriggerExtort() {
        harness.addToBattlefield(player1, new TitheDrinker());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new KraulWarrior()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Extort can be paid with exactly one white mana")
    void payingExtortWithWhite() {
        harness.addToBattlefield(player1, new TitheDrinker());
        harness.setHand(player1, List.of(new KraulWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Casting Tithe Drinker does not trigger its own extort")
    void castingItselfDoesNotExtort() {
        harness.setHand(player1, List.of(new TitheDrinker()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Unblocked combat damage gains life through lifelink")
    void combatDamageGainsLife() {
        var attacker = harness.addToBattlefieldAndReturn(player1, new TitheDrinker());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Extort cannot be paid with green mana")
    void wrongColorCannotPayExtort() {
        harness.addToBattlefield(player1, new TitheDrinker());
        harness.setHand(player1, List.of(new KraulWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
