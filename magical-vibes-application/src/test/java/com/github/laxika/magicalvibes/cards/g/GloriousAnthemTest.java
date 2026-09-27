package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CanyonWildcat;
import com.github.laxika.magicalvibes.cards.c.CoralEel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CanyonWildcat.class, CoralEel.class, GloriousAnthem.class})
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
        harness.assertOnBattlefield(player1, "Glorious Anthem");
    }

    // ===== Static effect: buffs own creatures =====

    @Test
    @DisplayName("Own creatures get +1/+1")
    void buffsOwnCreatures() {
        Permanent eel = harness.addToBattlefieldAndReturn(player1, new CoralEel());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertThat(gqs.getEffectivePower(gd, eel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, eel)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's creatures")
    void doesNotBuffOpponentCreatures() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent opponentEel = harness.addToBattlefieldAndReturn(player2, new CoralEel());

        assertThat(gqs.getEffectivePower(gd, opponentEel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentEel)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not buff creatures controlled by another player")
    void doesNotBuffCreaturesControlledByAnotherPlayer() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        Permanent eel = harness.addToBattlefieldAndReturn(player1, new CoralEel());

        assertThat(gqs.getEffectivePower(gd, eel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, eel)).isEqualTo(1);
    }

    @Test
    @DisplayName("Buffs all own creatures regardless of subtype")
    void buffsAllOwnCreaturesRegardlessOfSubtype() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new CoralEel());
        harness.addToBattlefield(player1, new CanyonWildcat());

        Permanent eel = findPermanent(player1, "Coral Eel");
        Permanent wildcat = findPermanent(player1, "Canyon Wildcat");

        assertThat(gqs.getEffectivePower(gd, eel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, eel)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, wildcat)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wildcat)).isEqualTo(2);
    }

    @Test
    @DisplayName("Buffs all own creatures regardless of subtype")
    void buffsAllOwnCreaturesRegardlessOfSubtypeUpstreamReview() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent eel = harness.addToBattlefieldAndReturn(player1, new CoralEel());
        Permanent wildcat = harness.addToBattlefieldAndReturn(player1, new CanyonWildcat());

        assertThat(gqs.getEffectivePower(gd, eel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, eel)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, wildcat)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wildcat)).isEqualTo(2);
    }

    // ===== Multiple sources =====

    @Test
    @DisplayName("Two Glorious Anthems give +2/+2")
    void twoAnthemsStack() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent eel = harness.addToBattlefieldAndReturn(player1, new CoralEel());

        assertThat(gqs.getEffectivePower(gd, eel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, eel)).isEqualTo(3);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Glorious Anthem leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent eel = harness.addToBattlefieldAndReturn(player1, new CoralEel());

        assertThat(gqs.getEffectivePower(gd, eel)).isEqualTo(3);

        // Remove Glorious Anthem
        gd.playerBattlefields.get(player1.getId()).remove(anthem);

        assertThat(gqs.getEffectivePower(gd, eel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, eel)).isEqualTo(1);
    }

    // ===== Bonus applies on resolve =====

    @Test
    @DisplayName("Bonus applies when Glorious Anthem resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new CoralEel());

        Permanent eel = findPermanent(player1, "Coral Eel");

        assertThat(gqs.getEffectivePower(gd, eel)).isEqualTo(2);

        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, eel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, eel)).isEqualTo(2);
    }

    // ===== Bonus applies on resolve =====

    @Test
    @DisplayName("Bonus applies when Glorious Anthem resolves onto battlefield")
    void bonusAppliesOnResolveUpstreamReview() {
        Permanent eel = harness.addToBattlefieldAndReturn(player1, new CoralEel());

        assertThat(gqs.getEffectivePower(gd, eel)).isEqualTo(2);

        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, eel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, eel)).isEqualTo(2);
    }

    // ===== Static bonus survives end-of-turn reset =====

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent eel = harness.addToBattlefieldAndReturn(player1, new CoralEel());

        // Simulate a temporary spell boost
        eel.setPowerModifier(eel.getPowerModifier() + 3);
        assertThat(gqs.getEffectivePower(gd, eel)).isEqualTo(6); // 2 base + 3 spell + 1 static

        // Reset end-of-turn modifiers
        eel.resetModifiers();

        // Spell bonus gone, static bonus still computed
        assertThat(gqs.getEffectivePower(gd, eel)).isEqualTo(3); // 2 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, eel)).isEqualTo(2);
    }
}
