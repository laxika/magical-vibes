package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShimmeringWingsTest {

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
    @DisplayName("Shimmering Wings has correct card properties")
    void hasCorrectProperties() {
        ShimmeringWings card = new ShimmeringWings();

        assertThat(card.getName()).isEqualTo("Shimmering Wings");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.AURA);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(GrantKeywordToEnchantedCreatureEffect.class);
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{U}");
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst()).isInstanceOf(ReturnSelfToHandEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Shimmering Wings puts it on the stack as enchantment spell")
    void castingPutsOnStack() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new ShimmeringWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shimmering Wings");
    }

    @Test
    @DisplayName("Resolving Shimmering Wings attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new ShimmeringWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Shimmering Wings")
                        && p.getAttachedTo() != null
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Flying =====

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent wingsPerm = new Permanent(new ShimmeringWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(wingsPerm);

        assertThat(gs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Shimmering Wings does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent otherBears = new Permanent(new GrizzlyBears());
        otherBears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherBears);

        Permanent wingsPerm = new Permanent(new ShimmeringWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(wingsPerm);

        assertThat(gs.hasKeyword(gd, otherBears, Keyword.FLYING)).isFalse();
    }

    // ===== Activated ability: return to hand =====

    @Test
    @DisplayName("Activating {U} ability returns Shimmering Wings to owner's hand")
    void activateAbilityReturnsToHand() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent wingsPerm = new Permanent(new ShimmeringWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(wingsPerm);

        harness.addMana(player1, ManaColor.BLUE, 1);

        // Activate the return-to-hand ability (wings is at index 1)
        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        // Shimmering Wings should be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shimmering Wings"));
        // No longer on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Shimmering Wings"));
    }

    @Test
    @DisplayName("Creature loses flying after Shimmering Wings returns to hand")
    void creatureLosesFlyingAfterBounce() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent wingsPerm = new Permanent(new ShimmeringWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(wingsPerm);

        // Verify flying is granted
        assertThat(gs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);

        // Activate the return-to-hand ability
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        // Creature no longer has flying
        assertThat(gs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isFalse();
    }

    // ===== Re-cast after bounce =====

    @Test
    @DisplayName("Shimmering Wings can be re-cast after returning to hand")
    void canRecastAfterBounce() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent wingsPerm = new Permanent(new ShimmeringWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(wingsPerm);

        harness.addMana(player1, ManaColor.BLUE, 1);

        // Bounce it
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shimmering Wings"));

        // Re-cast it
        harness.addMana(player1, ManaColor.BLUE, 1);
        int wingsIndex = -1;
        List<?> hand = gd.playerHands.get(player1.getId());
        for (int i = 0; i < hand.size(); i++) {
            if (((com.github.laxika.magicalvibes.model.Card) hand.get(i)).getName().equals("Shimmering Wings")) {
                wingsIndex = i;
                break;
            }
        }
        gs.playCard(gd, player1, wingsIndex, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        // Should be attached again
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Shimmering Wings")
                        && p.getAttachedTo() != null
                        && p.getAttachedTo().equals(bearsPerm.getId()));
        assertThat(gs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Shimmering Wings fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new ShimmeringWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        // Remove the target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Shimmering Wings should go to graveyard (fizzle)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shimmering Wings"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Shimmering Wings"));
    }

    // ===== Orphaned aura =====

    @Test
    @DisplayName("Shimmering Wings goes to graveyard when enchanted creature dies")
    void goesToGraveyardWhenCreatureDies() {
        // Player2 has a 2/2 creature with Shimmering Wings attached
        Permanent bearsPerm = new Permanent(new GrizzlyBears()); // 2/2
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        Permanent wingsPerm = new Permanent(new ShimmeringWings());
        wingsPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(wingsPerm);

        // Player1 has a 2/2 attacker — enough to kill the enchanted creature
        Permanent attackerPerm = new Permanent(new GrizzlyBears());
        attackerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(attackerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        gs.declareAttackers(gd, player1, List.of(0));

        // Enchanted creature blocks and dies
        gd.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Shimmering Wings should be in graveyard (orphaned aura cleanup)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Shimmering Wings"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shimmering Wings"));
    }
}
