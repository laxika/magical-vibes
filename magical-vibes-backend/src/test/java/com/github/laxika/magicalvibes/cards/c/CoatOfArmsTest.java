package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenCloudchaser;
import com.github.laxika.magicalvibes.cards.b.BallistaSquad;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostBySharedCreatureTypeEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoatOfArmsTest {

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
    @DisplayName("Coat of Arms has correct card properties")
    void hasCorrectProperties() {
        CoatOfArms card = new CoatOfArms();

        assertThat(card.getName()).isEqualTo("Coat of Arms");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{5}");
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(BoostBySharedCreatureTypeEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new CoatOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Coat of Arms");
    }

    @Test
    @DisplayName("Resolving puts Coat of Arms onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new CoatOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Coat of Arms"));
    }

    // ===== No bonus when creatures don't share a type =====

    @Test
    @DisplayName("No bonus for creatures that don't share a creature type")
    void noBonusForUnrelatedCreatures() {
        // GrizzlyBears (Bear) and AvenCloudchaser (Bird Soldier) share no types
        harness.addToBattlefield(player1, new CoatOfArms());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AvenCloudchaser());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        Permanent aven = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Aven Cloudchaser"))
                .findFirst().orElseThrow();

        assertThat(gs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gs.getEffectivePower(gd, aven)).isEqualTo(2);
        assertThat(gs.getEffectiveToughness(gd, aven)).isEqualTo(2);
    }

    // ===== Bonus for creatures sharing a type =====

    @Test
    @DisplayName("Two creatures sharing a type each get +1/+1")
    void twoCreaturesSharingType() {
        // Two GrizzlyBears (Bear, 2/2) share the Bear type
        harness.addToBattlefield(player1, new CoatOfArms());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        List<Permanent> bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .toList();

        assertThat(bears).hasSize(2);
        for (Permanent bear : bears) {
            // Each shares Bear with the other → +1/+1
            assertThat(gs.getEffectivePower(gd, bear)).isEqualTo(3);
            assertThat(gs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Three creatures sharing a type each get +2/+2")
    void threeCreaturesSharingType() {
        // Three GrizzlyBears (Bear, 2/2) all share Bear
        harness.addToBattlefield(player1, new CoatOfArms());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        List<Permanent> bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .toList();

        assertThat(bears).hasSize(3);
        for (Permanent bear : bears) {
            // Each shares Bear with 2 others → +2/+2
            assertThat(gs.getEffectivePower(gd, bear)).isEqualTo(4);
            assertThat(gs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("Creatures with partial type overlap get different bonuses")
    void partialTypeOverlap() {
        // BallistaSquad (Human Rebel, 2/4) and BenalishKnight (Human Knight, 2/2) share Human
        // AvenCloudchaser (Bird Soldier, 2/2) shares no type with either
        harness.addToBattlefield(player1, new CoatOfArms());
        harness.addToBattlefield(player1, new BallistaSquad());
        harness.addToBattlefield(player1, new BenalishKnight());
        harness.addToBattlefield(player1, new AvenCloudchaser());

        Permanent ballista = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ballista Squad"))
                .findFirst().orElseThrow();
        Permanent knight = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Benalish Knight"))
                .findFirst().orElseThrow();
        Permanent aven = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Aven Cloudchaser"))
                .findFirst().orElseThrow();

        // Ballista shares Human with Benalish → +1/+1
        assertThat(gs.getEffectivePower(gd, ballista)).isEqualTo(3);
        assertThat(gs.getEffectiveToughness(gd, ballista)).isEqualTo(3);
        // Benalish shares Human with Ballista → +1/+1
        assertThat(gs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gs.getEffectiveToughness(gd, knight)).isEqualTo(3);
        // Aven shares no type with either → +0/+0
        assertThat(gs.getEffectivePower(gd, aven)).isEqualTo(2);
        assertThat(gs.getEffectiveToughness(gd, aven)).isEqualTo(2);
    }

    // ===== Applies across both players' battlefields =====

    @Test
    @DisplayName("Bonus counts creatures on opponent's battlefield too")
    void bonusCountsOpponentCreatures() {
        // GrizzlyBears (Bear) on each side of the battlefield
        harness.addToBattlefield(player1, new CoatOfArms());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ownBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        Permanent opponentBears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Share Bear across battlefields → +1/+1 each
        assertThat(gs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gs.getEffectivePower(gd, opponentBears)).isEqualTo(3);
        assertThat(gs.getEffectiveToughness(gd, opponentBears)).isEqualTo(3);
    }

    // ===== Changeling interaction =====

    @Test
    @DisplayName("Changeling shares a creature type with every typed creature")
    void changelingSharesTypeWithEverything() {
        // ChangelingWayfinder (Shapeshifter, Changeling, 1/2) shares all creature types
        harness.addToBattlefield(player1, new CoatOfArms());
        harness.addToBattlefield(player1, new ChangelingWayfinder());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent wayfinder = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Changeling Wayfinder"))
                .findFirst().orElseThrow();
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Changeling shares Bear with Grizzly Bears → each gets +1/+1
        assertThat(gs.getEffectivePower(gd, wayfinder)).isEqualTo(2);  // 1 base + 1
        assertThat(gs.getEffectiveToughness(gd, wayfinder)).isEqualTo(3); // 2 base + 1
        assertThat(gs.getEffectivePower(gd, bears)).isEqualTo(3);     // 2 base + 1
        assertThat(gs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Two changelings share creature types with each other")
    void twoChangelingsShareTypes() {
        harness.addToBattlefield(player1, new CoatOfArms());
        harness.addToBattlefield(player1, new ChangelingWayfinder());
        harness.addToBattlefield(player1, new ChangelingWayfinder());

        List<Permanent> wayfinders = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Changeling Wayfinder"))
                .toList();

        assertThat(wayfinders).hasSize(2);
        for (Permanent wayfinder : wayfinders) {
            // Each changeling shares types with the other → +1/+1
            assertThat(gs.getEffectivePower(gd, wayfinder)).isEqualTo(2);  // 1 base + 1
            assertThat(gs.getEffectiveToughness(gd, wayfinder)).isEqualTo(3); // 2 base + 1
        }
    }

    @Test
    @DisplayName("Changeling gets bonus from every typed creature on the battlefield")
    void changelingBonusScalesWithAllCreatures() {
        // Changeling with GrizzlyBears (Bear) + AvenCloudchaser (Bird Soldier) + BenalishKnight (Human Knight)
        harness.addToBattlefield(player1, new CoatOfArms());
        harness.addToBattlefield(player1, new ChangelingWayfinder());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AvenCloudchaser());
        harness.addToBattlefield(player1, new BenalishKnight());

        Permanent wayfinder = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Changeling Wayfinder"))
                .findFirst().orElseThrow();

        // Changeling shares types with all 3 other creatures → +3/+3
        assertThat(gs.getEffectivePower(gd, wayfinder)).isEqualTo(4);     // 1 base + 3
        assertThat(gs.getEffectiveToughness(gd, wayfinder)).isEqualTo(5); // 2 base + 3
    }

    // ===== Coat of Arms itself is not a creature =====

    @Test
    @DisplayName("Single creature on battlefield gets no bonus")
    void singleCreatureNoBonus() {
        harness.addToBattlefield(player1, new CoatOfArms());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Only creature on battlefield, no shared types possible
        assertThat(gs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Bonus removed when Coat of Arms leaves =====

    @Test
    @DisplayName("Bonus is removed when Coat of Arms leaves the battlefield")
    void bonusRemovedWhenCoatLeaves() {
        harness.addToBattlefield(player1, new CoatOfArms());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // With Coat: shares Bear → +1/+1
        assertThat(gs.getEffectivePower(gd, bears)).isEqualTo(3);

        // Remove Coat of Arms
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Coat of Arms"));

        // Bonus gone
        assertThat(gs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Bonus applies on resolve =====

    @Test
    @DisplayName("Bonus applies when Coat of Arms resolves onto the battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CoatOfArms()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Before casting, no bonus
        assertThat(gs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // After resolving, bears share Bear → +1/+1
        assertThat(gs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    // ===== Static bonus survives end-of-turn reset =====

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new CoatOfArms());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Simulate a temporary spell boost
        bears.setPowerModifier(bears.getPowerModifier() + 3);
        assertThat(gs.getEffectivePower(gd, bears)).isEqualTo(6); // 2 base + 3 spell + 1 static

        // Reset end-of-turn modifiers
        bears.resetModifiers();

        // Spell bonus gone, static bonus still computed
        assertThat(gs.getEffectivePower(gd, bears)).isEqualTo(3); // 2 base + 1 static
        assertThat(gs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    // ===== Bonus updates dynamically as creatures enter/leave =====

    @Test
    @DisplayName("Bonus increases when a new creature sharing a type enters")
    void bonusIncreasesWhenNewCreatureEnters() {
        harness.addToBattlefield(player1, new CoatOfArms());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Shares Bear with 1 other → +1/+1
        assertThat(gs.getEffectivePower(gd, bears)).isEqualTo(3);

        // Add AvenCloudchaser (Bird Soldier) — does NOT share Bear
        harness.addToBattlefield(player1, new AvenCloudchaser());
        assertThat(gs.getEffectivePower(gd, bears)).isEqualTo(3); // unchanged

        // Add another GrizzlyBears — shares Bear
        harness.addToBattlefield(player1, new GrizzlyBears());
        assertThat(gs.getEffectivePower(gd, bears)).isEqualTo(4); // now +2/+2
    }

    @Test
    @DisplayName("Bonus decreases when a creature sharing a type leaves")
    void bonusDecreasesWhenCreatureLeaves() {
        harness.addToBattlefield(player1, new CoatOfArms());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // Shares Bear with 2 others → +2/+2
        assertThat(gs.getEffectivePower(gd, bears)).isEqualTo(4);

        // Remove one GrizzlyBears (not the one we're tracking)
        List<Permanent> allBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .toList();
        gd.playerBattlefields.get(player1.getId()).remove(allBears.get(1));

        // Now shares Bear with 1 other → +1/+1
        assertThat(gs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }
}
