package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDoesntUntapEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DehydrationTest {

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
    @DisplayName("Dehydration has correct card properties")
    void hasCorrectProperties() {
        Dehydration card = new Dehydration();

        assertThat(card.getName()).isEqualTo("Dehydration");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{3}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.AURA);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getCardText()).isEqualTo("Enchant creature\nEnchanted creature doesn't untap during its controller's untap step.");
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(EnchantedCreatureDoesntUntapEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Dehydration puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Dehydration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Dehydration");
    }

    @Test
    @DisplayName("Resolving Dehydration attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Dehydration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dehydration")
                        && p.getAttachedTo() != null
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Cannot cast Dehydration without enough mana")
    void cannotCastWithoutEnoughMana() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Dehydration()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Prevents untapping =====

    @Test
    @DisplayName("Tapped creature with Dehydration does not untap during controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        // Player2 has a tapped creature
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        bearsPerm.tap();
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        // Attach Dehydration to the creature
        Permanent dehydrationPerm = new Permanent(new Dehydration());
        dehydrationPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(dehydrationPerm);

        // Advance to player2's turn to trigger untap
        advanceToNextTurn(player1);

        // The creature should still be tapped
        assertThat(bearsPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untapped creature with Dehydration remains untapped (Dehydration does not tap)")
    void untappedCreatureRemainsUntapped() {
        // Player2 has an untapped creature with Dehydration
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        Permanent dehydrationPerm = new Permanent(new Dehydration());
        dehydrationPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(dehydrationPerm);

        // Advance to player2's turn
        advanceToNextTurn(player1);

        // Creature was untapped and stays untapped (Dehydration only prevents untapping)
        assertThat(bearsPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Other permanents owned by the same player still untap normally")
    void otherPermanentsStillUntap() {
        // Player2 has two tapped creatures, one with Dehydration
        Permanent enchantedBears = new Permanent(new GrizzlyBears());
        enchantedBears.setSummoningSick(false);
        enchantedBears.tap();
        gd.playerBattlefields.get(player2.getId()).add(enchantedBears);

        Permanent freeBears = new Permanent(new GrizzlyBears());
        freeBears.setSummoningSick(false);
        freeBears.tap();
        gd.playerBattlefields.get(player2.getId()).add(freeBears);

        // Attach Dehydration only to the first creature
        Permanent dehydrationPerm = new Permanent(new Dehydration());
        dehydrationPerm.setAttachedTo(enchantedBears.getId());
        gd.playerBattlefields.get(player1.getId()).add(dehydrationPerm);

        // Advance to player2's turn
        advanceToNextTurn(player1);

        // Enchanted creature stays tapped, free creature untaps
        assertThat(enchantedBears.isTapped()).isTrue();
        assertThat(freeBears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature stays tapped across multiple turns")
    void creatureStaysTappedAcrossMultipleTurns() {
        // Player2 has a tapped creature with Dehydration
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        bearsPerm.tap();
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        Permanent dehydrationPerm = new Permanent(new Dehydration());
        dehydrationPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(dehydrationPerm);

        // Advance through player2's turn
        advanceToNextTurn(player1);
        assertThat(bearsPerm.isTapped()).isTrue();

        // Advance through player1's turn
        advanceToNextTurn(player2);
        assertThat(bearsPerm.isTapped()).isTrue();

        // Advance through player2's turn again — still tapped
        advanceToNextTurn(player1);
        assertThat(bearsPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Multiple Dehydrations on different creatures prevent both from untapping")
    void multipleDehydrationsOnDifferentCreatures() {
        // Player2 has two tapped creatures
        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        bears1.tap();
        gd.playerBattlefields.get(player2.getId()).add(bears1);

        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        bears2.tap();
        gd.playerBattlefields.get(player2.getId()).add(bears2);

        // Attach a Dehydration to each
        Permanent dehydration1 = new Permanent(new Dehydration());
        dehydration1.setAttachedTo(bears1.getId());
        gd.playerBattlefields.get(player1.getId()).add(dehydration1);

        Permanent dehydration2 = new Permanent(new Dehydration());
        dehydration2.setAttachedTo(bears2.getId());
        gd.playerBattlefields.get(player1.getId()).add(dehydration2);

        // Advance to player2's turn
        advanceToNextTurn(player1);

        // Both creatures should remain tapped
        assertThat(bears1.isTapped()).isTrue();
        assertThat(bears2.isTapped()).isTrue();
    }

    // ===== Removal restores untapping =====

    @Test
    @DisplayName("Creature can untap again after Dehydration is removed")
    void creatureUntapsAfterDehydrationRemoved() {
        // Player2 has a tapped creature with Dehydration
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        bearsPerm.tap();
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        Permanent dehydrationPerm = new Permanent(new Dehydration());
        dehydrationPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(dehydrationPerm);

        // Remove Dehydration
        gd.playerBattlefields.get(player1.getId()).remove(dehydrationPerm);

        // Advance to player2's turn
        advanceToNextTurn(player1);

        // Creature should now untap normally
        assertThat(bearsPerm.isTapped()).isFalse();
    }

    // ===== Dehydration on own creature =====

    @Test
    @DisplayName("Dehydration can be cast on own creature")
    void canCastOnOwnCreature() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Dehydration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dehydration")
                        && p.getAttachedTo() != null
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Dehydration on own creature prevents it from untapping")
    void dehydrationOnOwnCreaturePreventsUntap() {
        // Player1 has a tapped creature with their own Dehydration on it
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        bearsPerm.tap();
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent dehydrationPerm = new Permanent(new Dehydration());
        dehydrationPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(dehydrationPerm);

        // Advance to player1's turn
        advanceToNextTurn(player2);

        // Player1's creature should still be tapped
        assertThat(bearsPerm.isTapped()).isTrue();
    }

    // ===== Dehydration fizzles if target removed =====

    @Test
    @DisplayName("Dehydration fizzles to graveyard if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Dehydration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        // Remove the target before Dehydration resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Dehydration should be in graveyard, not on battlefield
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dehydration"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Dehydration"));
    }

    // ===== Full integration: cast, resolve, advance turn =====

    @Test
    @DisplayName("Full integration: cast Dehydration on tapped creature, advance turn, creature stays tapped")
    void fullIntegrationCastAndPreventUntap() {
        // Player2 has a tapped creature
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        bearsPerm.tap();
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        // Player1 casts Dehydration on it
        harness.setHand(player1, List.of(new Dehydration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        // Verify Dehydration resolved and attached
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dehydration")
                        && p.getAttachedTo() != null
                        && p.getAttachedTo().equals(bearsPerm.getId()));

        // Advance to player2's turn (triggers untap step)
        advanceToNextTurn(player1);

        // Creature should still be tapped due to Dehydration
        assertThat(bearsPerm.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        // Clear hands so cleanup hand-size limit doesn't interrupt turn advancement
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }
}

