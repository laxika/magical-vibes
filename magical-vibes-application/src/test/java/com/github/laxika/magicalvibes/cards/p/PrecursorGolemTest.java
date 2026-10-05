package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AgonyWarp;
import com.github.laxika.magicalvibes.cards.d.Disembowel;
import com.github.laxika.magicalvibes.cards.f.ForkedBolt;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrecursorGolem.class, Shock.class, GrizzlyBears.class, AgonyWarp.class, ForkedBolt.class, Disembowel.class})
class PrecursorGolemTest extends BaseCardTest {

    

    @Test
    @DisplayName("ETB creates two 3/3 colorless Golem artifact creature tokens")
    void etbCreatesTwoGolemTokens() {
        castAndResolveGolemWithTokens();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(3); // Precursor Golem + 2 tokens
        assertThat(countGolemTokens(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting instant targeting a Golem triggers Precursor Golem's ability")
    void spellTargetingGolemTriggers() {
        castAndResolveGolemWithTokens();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID golemTokenId = getAnyGolemTokenId(player1);

        harness.castInstant(player1, 0, golemTokenId);

        // Stack: Shock (bottom) + triggered ability from Precursor Golem (top)
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Precursor Golem");
    }

    @Test
    @DisplayName("Triggered ability creates copies targeting each other Golem")
    void triggeredAbilityCreatesCopies() {
        castAndResolveGolemWithTokens();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID golemTokenId = getAnyGolemTokenId(player1);

        harness.castInstant(player1, 0, golemTokenId);
        harness.passBothPriorities(); // resolve triggered ability → creates 2 copies

        // Stack: original Shock (bottom) + 2 copies (top)
        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
        assertThat(gd.stack.get(1).isCopy()).isTrue();
        assertThat(gd.stack.get(1).getCard().getName()).isEqualTo("Shock");
        assertThat(gd.stack.get(2).isCopy()).isTrue();
        assertThat(gd.stack.get(2).getCard().getName()).isEqualTo("Shock");
    }

    @Test
    @DisplayName("All copies and original Shock resolve — all Golems survive 2 damage on 3/3")
    void allCopiesAndOriginalResolve() {
        castAndResolveGolemWithTokens();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID golemTokenId = getAnyGolemTokenId(player1);

        harness.castInstant(player1, 0, golemTokenId);
        harness.passBothPriorities(); // resolve triggered ability
        harness.passBothPriorities(); // resolve first copy
        harness.passBothPriorities(); // resolve second copy
        harness.passBothPriorities(); // resolve original Shock

        assertThat(gd.stack).isEmpty();
        // All 3 Golems survive (2 damage < 3 toughness)
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Opponent casting a spell targeting a Golem also triggers copies")
    void opponentSpellTargetingGolemTriggersCopies() {
        castAndResolveGolemWithTokens();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID golemTokenId = getAnyGolemTokenId(player1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, golemTokenId);

        // Triggered ability fires for opponent's spell too
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("No trigger when spell targets a player")
    void noTriggerWhenTargetingPlayer() {
        castAndResolveGolemWithTokens();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }

    @Test
    @DisplayName("No trigger when spell targets a non-Golem creature")
    void noTriggerWhenTargetingNonGolem() {
        castAndResolveGolemWithTokens();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, bearsId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }

    @Test
    @DisplayName("Each copy targets a different Golem")
    void eachCopyTargetsDifferentGolem() {
        castAndResolveGolemWithTokens();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID golemTokenId = getAnyGolemTokenId(player1);

        harness.castInstant(player1, 0, golemTokenId);
        harness.passBothPriorities(); // resolve triggered ability

        // Collect all target IDs
        List<UUID> allTargets = gd.stack.stream()
                .map(se -> se.getTargetId())
                .toList();

        // All 3 Golems should be targeted (original + 2 copies)
        assertThat(allTargets).hasSize(3);
        assertThat(allTargets).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("Agony Warp targeting one Golem twice copies both targets onto each other Golem")
    void repeatedTargetsAreReplacedOnEveryCopy() {
        castAndResolveGolemWithTokens();
        UUID originalTarget = getAnyGolemTokenId(player1);
        harness.setHand(player1, List.of(new AgonyWarp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, List.of(originalTarget, originalTarget));
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Forked Bolt assigned entirely to one Golem deals two damage to each Golem")
    void dividedDamageCopiesRetargetTheirAssignments() {
        castAndResolveGolemWithTokens();
        harness.setHand(player1, List.of(new ForkedBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, Map.of(getAnyGolemTokenId(player1), 2));
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(3)
                .allSatisfy(golem -> assertThat(golem.getMarkedDamage()).isEqualTo(2));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A spell targeting two different Golems does not trigger copying")
    void distinctGolemTargetsDoNotTrigger() {
        castAndResolveGolemWithTokens();
        List<UUID> targets = gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getId).limit(2).toList();
        harness.setHand(player1, List.of(new AgonyWarp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, targets);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Disembowel copies use the original X to determine eligible Golems")
    void eligibleTargetsUseOriginalXValue() {
        castAndResolveGolemWithTokens();
        UUID originalTarget = harness.getPermanentId(player1, "Precursor Golem");
        Permanent otherGolem = harness.addToBattlefieldAndReturn(player2, new PrecursorGolem());
        harness.setHand(player1, List.of(new Disembowel()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0, 5, originalTarget);
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(entry -> entry.isCopy()).toList())
                .singleElement()
                .satisfies(copy -> {
                    assertThat(copy.getTargetId()).isEqualTo(otherGolem.getId());
                    assertThat(copy.getXValue()).isEqualTo(5);
                });
    }

@Test
    @DisplayName("Copies of an opponent's spell remain controlled by that opponent")
    void opponentControlsEveryCopy() {
        castAndResolveGolemWithTokens();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, getAnyGolemTokenId(player1));
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack)
                .allSatisfy(entry -> assertThat(entry.getControllerId()).isEqualTo(player2.getId()));
        assertThat(gd.stack.stream().filter(entry -> entry.isCopy()).toList()).hasSize(2);
    }

    private void castAndResolveGolemWithTokens() {
        harness.castFromHand(player1, new PrecursorGolem(), "{5}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
    }

    private int countGolemTokens(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Golem"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.GOLEM))
                .count();
    }

    private UUID getAnyGolemTokenId(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Golem"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Golem token found"))
                .getId();
    }
}
