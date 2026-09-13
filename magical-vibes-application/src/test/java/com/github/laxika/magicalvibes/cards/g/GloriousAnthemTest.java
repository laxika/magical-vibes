package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.PouncingJaguar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GloriousAnthem.class, GorillaWarrior.class, PouncingJaguar.class})
class GloriousAnthemTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving puts Glorious Anthem onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GloriousAnthem);
    }

    // ===== Static effect: buffs own creatures =====

    @Test
    @DisplayName("Own creatures get +1/+1")
    void buffsOwnCreatures() {
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertThat(gqs.getEffectivePower(gd, gorilla)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gorilla)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff opponent's creatures")
    void doesNotBuffOpponentCreatures() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent opponentGorilla = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());

        assertThat(gqs.getEffectivePower(gd, opponentGorilla)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentGorilla)).isEqualTo(2);
    }

    @Test
    @DisplayName("Buffs all own creatures regardless of subtype")
    void buffsAllOwnCreaturesRegardlessOfSubtype() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());
        Permanent jaguar = harness.addToBattlefieldAndReturn(player1, new PouncingJaguar());

        assertThat(gqs.getEffectivePower(gd, gorilla)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gorilla)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, jaguar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, jaguar)).isEqualTo(3);
    }

    // ===== Multiple sources =====

    @Test
    @DisplayName("Two Glorious Anthems give +2/+2")
    void twoAnthemsStack() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());

        assertThat(gqs.getEffectivePower(gd, gorilla)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, gorilla)).isEqualTo(4);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Glorious Anthem leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());

        assertThat(gqs.getEffectivePower(gd, gorilla)).isEqualTo(4);

        // Remove Glorious Anthem
        gd.playerBattlefields.get(player1.getId()).remove(anthem);

        assertThat(gqs.getEffectivePower(gd, gorilla)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gorilla)).isEqualTo(2);
    }

    // ===== Bonus applies on resolve =====

    @Test
    @DisplayName("Bonus applies when Glorious Anthem resolves onto battlefield")
    void bonusAppliesOnResolve() {
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());

        assertThat(gqs.getEffectivePower(gd, gorilla)).isEqualTo(3);

        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gorilla)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gorilla)).isEqualTo(3);
    }

    // ===== Static bonus survives end-of-turn reset =====

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());

        // Simulate a temporary spell boost
        gorilla.setPowerModifier(gorilla.getPowerModifier() + 3);
        assertThat(gqs.getEffectivePower(gd, gorilla)).isEqualTo(7); // 3 base + 3 spell + 1 static

        // Reset end-of-turn modifiers
        gorilla.resetModifiers();

        // Spell bonus gone, static bonus still computed
        assertThat(gqs.getEffectivePower(gd, gorilla)).isEqualTo(4); // 3 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, gorilla)).isEqualTo(3);
    }
}

