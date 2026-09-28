package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.cards.c.CanyonWildcat;
import com.github.laxika.magicalvibes.cards.c.CoralEel;

@CardUsed({CrawWurm.class, GloriousAnthem.class, GrizzlyBears.class, CanyonWildcat.class, CoralEel.class})
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
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not buff opponent's creatures")
    void doesNotBuffOpponentCreatures() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Buffs all own creatures regardless of subtype")
    void buffsAllOwnCreaturesRegardlessOfSubtype() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Buffs all own creatures regardless of subtype")
    void buffsAllOwnCreaturesRegardlessOfSubtypeUpstreamReview() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(5);
    }

    // ===== Multiple sources =====

    @Test
    @DisplayName("Two Glorious Anthems give +2/+2")
    void twoAnthemsStack() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(6);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Glorious Anthem leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(7);

        // Remove Glorious Anthem
        gd.playerBattlefields.get(player1.getId()).remove(anthem);

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(4);
    }

    // ===== Bonus applies on resolve =====

    @Test
    @DisplayName("Bonus applies when Glorious Anthem resolves onto battlefield")
    void bonusAppliesOnResolve() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    // ===== Bonus applies on resolve =====

    @Test
    @DisplayName("Bonus applies when Glorious Anthem resolves onto battlefield")
    void bonusAppliesOnResolveUpstreamReview() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(6);

        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(5);
    }

    // ===== Static bonus survives end-of-turn reset =====

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new CrawWurm());

        // Simulate a temporary spell boost
        wurm.setPowerModifier(wurm.getPowerModifier() + 3);
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(10); // 6 base + 3 spell + 1 static

        // Reset end-of-turn modifiers
        wurm.resetModifiers();

        // Spell bonus gone, static bonus still computed
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(7); // 6 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not buff creatures controlled by another player")
    void doesNotBuffCreaturesControlledByAnotherPlayer() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        Permanent eel = harness.addToBattlefieldAndReturn(player1, new CoralEel());

        assertThat(gqs.getEffectivePower(gd, eel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, eel)).isEqualTo(1);
    }
}
