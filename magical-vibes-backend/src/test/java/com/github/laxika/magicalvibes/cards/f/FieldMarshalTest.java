package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesBySubtypeEffect;
import com.github.laxika.magicalvibes.cards.a.AvenCloudchaser;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FieldMarshalTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Field Marshal has correct card properties")
    void hasCorrectProperties() {
        FieldMarshal card = new FieldMarshal();

        assertThat(card.getName()).isEqualTo("Field Marshal");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{W}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(card.getStaticEffects()).hasSize(1);
        assertThat(card.getStaticEffects().getFirst()).isInstanceOf(BoostCreaturesBySubtypeEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Field Marshal puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new FieldMarshal()));
        harness.addMana(player1, "W", 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Field Marshal");
    }

    @Test
    @DisplayName("Resolving puts Field Marshal onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new FieldMarshal()));
        harness.addMana(player1, "W", 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Field Marshal"));
    }

    @Test
    @DisplayName("Field Marshal enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new FieldMarshal()));
        harness.addMana(player1, "W", 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Field Marshal"))
                .findFirst().orElseThrow();
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== Static effect: buffs other Soldiers =====

    @Test
    @DisplayName("Other Soldier creatures get +1/+1 and first strike")
    void buffsOtherSoldiers() {
        // Aven Cloudchaser is a Bird Soldier (2/2 flying)
        harness.addToBattlefield(player1, new AvenCloudchaser());
        harness.addToBattlefield(player1, new FieldMarshal());

        Permanent cloudchaser = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Aven Cloudchaser"))
                .findFirst().orElseThrow();

        assertThat(gs.getEffectivePower(gd, cloudchaser)).isEqualTo(3);
        assertThat(gs.getEffectiveToughness(gd, cloudchaser)).isEqualTo(3);
        assertThat(gs.hasKeyword(gd, cloudchaser, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Field Marshal does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new FieldMarshal());

        Permanent marshal = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Field Marshal"))
                .findFirst().orElseThrow();

        assertThat(gs.getEffectivePower(gd, marshal)).isEqualTo(2);
        assertThat(gs.getEffectiveToughness(gd, marshal)).isEqualTo(2);
        assertThat(gs.hasKeyword(gd, marshal, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Soldier creatures")
    void doesNotBuffNonSoldiers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FieldMarshal());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Buffs opponent's Soldier creatures too")
    void buffsOpponentSoldiers() {
        harness.addToBattlefield(player1, new FieldMarshal());
        harness.addToBattlefield(player2, new AvenCloudchaser());

        Permanent opponentSoldier = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Aven Cloudchaser"))
                .findFirst().orElseThrow();

        assertThat(gs.getEffectivePower(gd, opponentSoldier)).isEqualTo(3);
        assertThat(gs.getEffectiveToughness(gd, opponentSoldier)).isEqualTo(3);
        assertThat(gs.hasKeyword(gd, opponentSoldier, Keyword.FIRST_STRIKE)).isTrue();
    }

    // ===== Multiple sources =====

    @Test
    @DisplayName("Two Field Marshals buff each other")
    void twoMarshalsBuffEachOther() {
        harness.addToBattlefield(player1, new FieldMarshal());
        harness.addToBattlefield(player1, new FieldMarshal());

        List<Permanent> marshals = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Field Marshal"))
                .toList();

        assertThat(marshals).hasSize(2);
        for (Permanent marshal : marshals) {
            // Each gets +1/+1 from the other → 3/3 with first strike
            assertThat(gs.getEffectivePower(gd, marshal)).isEqualTo(3);
            assertThat(gs.getEffectiveToughness(gd, marshal)).isEqualTo(3);
            assertThat(gs.hasKeyword(gd, marshal, Keyword.FIRST_STRIKE)).isTrue();
        }
    }

    @Test
    @DisplayName("Two Field Marshals give +2/+2 to other Soldiers")
    void twoMarshalsStackBonuses() {
        harness.addToBattlefield(player1, new FieldMarshal());
        harness.addToBattlefield(player1, new FieldMarshal());
        harness.addToBattlefield(player1, new AvenCloudchaser());

        Permanent cloudchaser = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Aven Cloudchaser"))
                .findFirst().orElseThrow();

        // 2/2 base + 2/2 from two sources = 4/4
        assertThat(gs.getEffectivePower(gd, cloudchaser)).isEqualTo(4);
        assertThat(gs.getEffectiveToughness(gd, cloudchaser)).isEqualTo(4);
        assertThat(gs.hasKeyword(gd, cloudchaser, Keyword.FIRST_STRIKE)).isTrue();
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Field Marshal leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new FieldMarshal());
        harness.addToBattlefield(player1, new AvenCloudchaser());

        Permanent cloudchaser = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Aven Cloudchaser"))
                .findFirst().orElseThrow();

        // Verify buff is applied
        assertThat(gs.getEffectivePower(gd, cloudchaser)).isEqualTo(3);

        // Remove Field Marshal from battlefield
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Field Marshal"));

        // Bonus should be gone immediately (computed on the fly)
        assertThat(gs.getEffectivePower(gd, cloudchaser)).isEqualTo(2);
        assertThat(gs.getEffectiveToughness(gd, cloudchaser)).isEqualTo(2);
        assertThat(gs.hasKeyword(gd, cloudchaser, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Bonus applied on resolve =====

    @Test
    @DisplayName("Bonus applies when Field Marshal resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new AvenCloudchaser());
        harness.setHand(player1, List.of(new FieldMarshal()));
        harness.addMana(player1, "W", 3);

        Permanent cloudchaser = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Aven Cloudchaser"))
                .findFirst().orElseThrow();

        // Before casting, no bonus
        assertThat(gs.getEffectivePower(gd, cloudchaser)).isEqualTo(2);

        // Cast and resolve Field Marshal
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // After resolving, Aven Cloudchaser should be buffed
        assertThat(gs.getEffectivePower(gd, cloudchaser)).isEqualTo(3);
        assertThat(gs.getEffectiveToughness(gd, cloudchaser)).isEqualTo(3);
        assertThat(gs.hasKeyword(gd, cloudchaser, Keyword.FIRST_STRIKE)).isTrue();
    }

    // ===== Combat with static buff =====

    @Test
    @DisplayName("Soldier with static buff uses boosted stats in combat")
    void soldierUsesBoostedStatsInCombat() {
        // Aven Cloudchaser (2/2 base) buffed by Field Marshal → 3/3 with first strike
        // Attacks into a 3/3 blocker → first strike kills blocker, Cloudchaser survives
        harness.addToBattlefield(player1, new FieldMarshal());

        AvenCloudchaser aven = new AvenCloudchaser();
        Permanent attacker = new Permanent(aven);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        GrizzlyBears big = new GrizzlyBears();
        big.setPower(3);
        big.setToughness(3);
        Permanent blocker = new Permanent(big);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.setBlockingTarget(1); // Cloudchaser is at index 1 (Marshal is 0)
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        gs.passPriority(gd, player1);
        gs.passPriority(gd, player2);

        // Cloudchaser (3/3 with first strike) deals 3 first strike damage → kills 3/3 blocker
        // Blocker dies before dealing regular damage → Cloudchaser survives
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Aven Cloudchaser"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Static bonus survives end-of-turn reset =====

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new FieldMarshal());
        harness.addToBattlefield(player1, new AvenCloudchaser());

        Permanent cloudchaser = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Aven Cloudchaser"))
                .findFirst().orElseThrow();

        // Simulate a temporary spell boost
        cloudchaser.setPowerModifier(cloudchaser.getPowerModifier() + 7);
        assertThat(gs.getEffectivePower(gd, cloudchaser)).isEqualTo(10); // 2 base + 7 spell + 1 static

        // Reset end-of-turn modifiers (simulates cleanup step)
        cloudchaser.resetModifiers();

        // Spell bonus gone, static bonus still computed
        assertThat(gs.getEffectivePower(gd, cloudchaser)).isEqualTo(3); // 2 base + 1 static
        assertThat(gs.getEffectiveToughness(gd, cloudchaser)).isEqualTo(3);
        assertThat(gs.hasKeyword(gd, cloudchaser, Keyword.FIRST_STRIKE)).isTrue();
    }
}
