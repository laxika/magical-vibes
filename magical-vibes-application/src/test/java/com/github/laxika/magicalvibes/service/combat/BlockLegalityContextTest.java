package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.e.ElvenRiders;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.s.SeveredLegion;
import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.cards.z.ZodiacMonkey;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.battlefield.BlockLegalityContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Rules-parity spec for the pairwise block-legality engine ({@code findBlockDenial} behind
 * {@link BlockLegalityContext}). Two layers of protection:
 *
 * <ul>
 *   <li>Per-rule behavioral tests pin the legality outcome AND the exact user-facing message
 *       for every denial reason, using real cards resolved through the engine.</li>
 *   <li>A parity matrix asserts that a shared (cached) context, fresh single-use contexts,
 *       and the message path all agree for every blocker × attacker pair — guarding the
 *       per-creature fact caches against cross-pair pollution and order dependence.</li>
 * </ul>
 */
class BlockLegalityContextTest extends BaseCardTest {

    private Permanent attacking(Player player, Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.setAttacking(true);
        return perm;
    }

    private List<Permanent> defenderBattlefield() {
        return gd.playerBattlefields.get(player2.getId());
    }

    private Optional<String> reason(Permanent blocker, Permanent attacker) {
        return gqs.getBlockingIllegalityReason(gd, blocker, attacker, defenderBattlefield());
    }

    // ===== Per-rule legality + exact message =====

    @Test
    @DisplayName("Flying attacker: ground creature denied with (flying) message, reach creature may block")
    void flyingEvasion() {
        Permanent sprite = attacking(player1, new CloudSprite());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        assertThat(reason(bears, sprite)).contains("Grizzly Bears cannot block Cloud Sprite (flying)");
        assertThat(reason(spider, sprite)).isEmpty();
    }

    @Test
    @DisplayName("Horsemanship attacker: only horsemanship creatures may block")
    void horsemanshipEvasion() {
        Permanent cavalry = attacking(player1, new ShuCavalry());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent defenderCavalry = addCreatureReady(player2, new ShuCavalry());

        assertThat(reason(bears, cavalry)).contains("Grizzly Bears cannot block Shu Cavalry (horsemanship)");
        assertThat(reason(defenderCavalry, cavalry)).isEmpty();
    }

    @Test
    @DisplayName("Fear attacker: non-black non-artifact denied, black creature may block")
    void fearEvasion() {
        Permanent legion = attacking(player1, new SeveredLegion());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent zombies = addCreatureReady(player2, new ScatheZombies());

        assertThat(reason(bears, legion)).contains("Grizzly Bears cannot block Severed Legion (fear)");
        assertThat(reason(zombies, legion)).isEmpty();
    }

    @Test
    @DisplayName("Blocker restricted to matching attackers (Cloud Sprite blocks only flyers)")
    void blockerLimitedToMatchingAttackers() {
        Permanent bears = attacking(player1, new GrizzlyBears());
        Permanent sprite = addCreatureReady(player2, new CloudSprite());

        assertThat(reason(sprite, bears)).contains("Cloud Sprite can only block creatures with flying");
    }

    @Test
    @DisplayName("Attacker blockable only by matching creatures (Elven Riders: flyers and Walls)")
    void attackerLimitedToMatchingBlockers() {
        Permanent riders = attacking(player1, new ElvenRiders());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent wall = addCreatureReady(player2, new WallOfAir());

        assertThat(reason(bears, riders)).contains("Elven Riders can only be blocked by creatures with flying or Walls");
        assertThat(reason(wall, riders)).isEmpty();
    }

    @Test
    @DisplayName("Landwalk: unblockable only while the defender controls a matching land")
    void landwalkEvasion() {
        Permanent monkey = attacking(player1, new ZodiacMonkey());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(reason(bears, monkey)).isEmpty();

        harness.addToBattlefield(player2, new Forest());
        assertThat(reason(bears, monkey)).contains("Zodiac Monkey can't be blocked (forestwalk)");
    }

    @Test
    @DisplayName("Protection: attacker with protection from white can't be blocked by a white creature")
    void protectionFromBlockerColor() {
        Permanent blackKnight = attacking(player1, new BlackKnight());
        Permanent whiteKnight = addCreatureReady(player2, new WhiteKnight());
        Permanent zombies = addCreatureReady(player2, new ScatheZombies());

        assertThat(reason(whiteKnight, blackKnight)).contains("White Knight cannot block Black Knight (protection)");
        assertThat(reason(zombies, blackKnight)).isEmpty();
    }

    @Test
    @DisplayName("Static \"can't block\" on the blocker")
    void blockerCantBlockStatic() {
        Permanent zombies = attacking(player1, new ScatheZombies());
        Permanent raider = addCreatureReady(player2, new GoblinRaider());

        assertThat(reason(raider, zombies)).contains("Goblin Raider can't block");
    }

    @Test
    @DisplayName("\"Can't block this turn\" flag on the blocker")
    void blockerCantBlockThisTurn() {
        Permanent zombies = attacking(player1, new ScatheZombies());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCantBlockThisTurn(true);

        assertThat(reason(bears, zombies)).contains("Grizzly Bears can't block this turn");
    }

    @Test
    @DisplayName("Per-attacker \"can't block that creature this turn\" restriction")
    void blockerCantBlockSpecificAttacker() {
        Permanent zombies = attacking(player1, new ScatheZombies());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.getCantBlockIds().add(zombies.getId());

        assertThat(reason(bears, zombies)).contains("Grizzly Bears can't block Scathe Zombies this turn");
        assertThat(reason(bears, zombies)).isPresent();

        Permanent otherAttacker = attacking(player1, new GrizzlyBears());
        assertThat(reason(bears, otherAttacker)).isEmpty();
    }

    @Test
    @DisplayName("\"Can't be blocked\" flag on the attacker")
    void attackerCantBeBlockedFlag() {
        Permanent zombies = attacking(player1, new ScatheZombies());
        zombies.setCantBeBlocked(true);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(reason(bears, zombies)).contains("Scathe Zombies can't be blocked");
    }

    // ===== Shared-context parity =====

    @Test
    @DisplayName("Shared context, fresh contexts, and the message path agree for every pair, in any query order")
    void sharedContextMatchesFreshContextForEveryPair() {
        List<Permanent> attackers = List.of(
                attacking(player1, new CloudSprite()),
                attacking(player1, new SeveredLegion()),
                attacking(player1, new ZodiacMonkey()),
                attacking(player1, new BlackKnight()),
                attacking(player1, new ElvenRiders()),
                attacking(player1, new ShuCavalry()),
                attacking(player1, new ScatheZombies()));
        // Flag-based restrictions so the matrix also exercises the non-card state paths
        attackers.get(6).setCantBeBlocked(true);

        harness.addToBattlefield(player2, new Forest());
        List<Permanent> blockers = List.of(
                addCreatureReady(player2, new GiantSpider()),
                addCreatureReady(player2, new ScatheZombies()),
                addCreatureReady(player2, new WhiteKnight()),
                addCreatureReady(player2, new GrizzlyBears()),
                addCreatureReady(player2, new GoblinRaider()),
                addCreatureReady(player2, new WallOfAir()));
        // Flip an otherwise-legal pair via the per-attacker restriction (black blocker vs fear)
        blockers.get(1).getCantBlockIds().add(attackers.get(1).getId());

        List<Permanent> defenderBattlefield = defenderBattlefield();
        BlockLegalityContext shared = gqs.createBlockLegalityContext(gd, defenderBattlefield);

        int legalPairs = 0;
        int illegalPairs = 0;
        for (Permanent attacker : attackers) {
            for (Permanent blocker : blockers) {
                Optional<String> fresh = gqs.getBlockingIllegalityReason(gd, blocker, attacker, defenderBattlefield);
                String pair = blocker.getCard().getName() + " blocking " + attacker.getCard().getName();

                assertThat(gqs.canBlockAttacker(shared, blocker, attacker))
                        .as("shared-context boolean vs fresh message path: %s", pair)
                        .isEqualTo(fresh.isEmpty());
                assertThat(gqs.getBlockingIllegalityReason(shared, blocker, attacker))
                        .as("shared-context message vs fresh message: %s", pair)
                        .isEqualTo(fresh);
                assertThat(gqs.canBlockAttacker(gd, blocker, attacker, defenderBattlefield))
                        .as("legacy boolean form vs fresh message path: %s", pair)
                        .isEqualTo(fresh.isEmpty());

                if (fresh.isEmpty()) legalPairs++; else illegalPairs++;
            }
        }
        // The board must exercise both outcomes, or the parity sweep proves nothing
        assertThat(legalPairs).isPositive();
        assertThat(illegalPairs).isPositive();

        // A second shared context queried in reverse order must agree with the first —
        // guards the lazy per-creature caches against query-order dependence.
        BlockLegalityContext reversed = gqs.createBlockLegalityContext(gd, defenderBattlefield);
        for (int a = attackers.size() - 1; a >= 0; a--) {
            for (int b = blockers.size() - 1; b >= 0; b--) {
                Permanent attacker = attackers.get(a);
                Permanent blocker = blockers.get(b);
                assertThat(gqs.canBlockAttacker(reversed, blocker, attacker))
                        .as("reverse-order context: %s blocking %s",
                                blocker.getCard().getName(), attacker.getCard().getName())
                        .isEqualTo(gqs.canBlockAttacker(shared, blocker, attacker));
            }
        }
    }

    @Test
    @DisplayName("computeLegalBlockPairs (shared-context production path) yields the per-pair matrix")
    void computeLegalBlockPairsUsesSharedContext() {
        Permanent sprite = attacking(player1, new CloudSprite());
        Permanent zombies = attacking(player1, new ScatheZombies());
        Permanent spider = addCreatureReady(player2, new GiantSpider());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        List<Permanent> attackerBattlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> defenderBattlefield = defenderBattlefield();
        int spriteIdx = attackerBattlefield.indexOf(sprite);
        int zombiesIdx = attackerBattlefield.indexOf(zombies);
        int spiderIdx = defenderBattlefield.indexOf(spider);
        int bearsIdx = defenderBattlefield.indexOf(bears);

        CombatBlockService combatBlockService = GameTestEngineContext.get().getBean(CombatBlockService.class);
        Map<Integer, List<Integer>> pairs = combatBlockService.computeLegalBlockPairs(gd,
                List.of(spiderIdx, bearsIdx), List.of(spriteIdx, zombiesIdx),
                player2.getId(), player1.getId());

        assertThat(pairs.get(spiderIdx)).containsExactlyInAnyOrder(spriteIdx, zombiesIdx);
        assertThat(pairs.get(bearsIdx)).containsExactly(zombiesIdx);
    }
}
