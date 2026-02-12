package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCastCostEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuraOfSilenceTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Aura of Silence has correct card properties")
    void hasCorrectProperties() {
        AuraOfSilence card = new AuraOfSilence();

        assertThat(card.getName()).isEqualTo("Aura of Silence");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{1}{W}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(IncreaseOpponentCastCostEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_SACRIFICE)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_SACRIFICE).getFirst())
                .isInstanceOf(DestroyTargetPermanentEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Aura of Silence puts it on the stack as an enchantment spell")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new AuraOfSilence()));
        harness.addMana(player1, "W", 3);

        harness.castEnchantment(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Aura of Silence");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Aura of Silence resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new AuraOfSilence()));
        harness.addMana(player1, "W", 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Aura of Silence"));
    }

    // ===== Static ability: cost increase =====

    @Test
    @DisplayName("Opponent's enchantment spells cost {2} more")
    void opponentEnchantmentsCostMore() {
        harness.addToBattlefield(player1, new AuraOfSilence());

        // Player2 tries to cast Angelic Chorus ({3}{W}{W} = 5 mana), needs 7 with Aura
        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AngelicChorus()));
        harness.addMana(player2, "W", 5);

        // 5 mana is not enough (needs 5 + 2 = 7)
        assertThatThrownBy(() -> harness.castEnchantment(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponent can still cast enchantments with enough mana to cover the increase")
    void opponentCanCastWithEnoughMana() {
        harness.addToBattlefield(player1, new AuraOfSilence());

        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AngelicChorus()));
        harness.addMana(player2, "W", 7);

        // 7 mana is enough (5 + 2 = 7)
        harness.castEnchantment(player2, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getCard().getName()).isEqualTo("Angelic Chorus");
        // All 7 mana should be spent (2W colored + 5 generic, including 2 extra)
        assertThat(harness.getGameData().playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Own enchantment spells are not affected by own Aura of Silence")
    void ownEnchantmentsNotAffected() {
        harness.addToBattlefield(player1, new AuraOfSilence());

        harness.setHand(player1, List.of(new AngelicChorus()));
        harness.addMana(player1, "W", 5);

        // Player1 can cast their own enchantment for normal cost
        harness.castEnchantment(player1, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Creature spells are not affected by cost increase")
    void creatureSpellsNotAffected() {
        harness.addToBattlefield(player1, new AuraOfSilence());

        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, "G", 2);

        // Grizzly Bears costs {1}{G} = 2 mana, no increase for creatures
        harness.castCreature(player2, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Two Auras of Silence stack the cost increase to {4}")
    void twoAurasStackCostIncrease() {
        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addToBattlefield(player1, new AuraOfSilence());

        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AuraOfSilence()));
        // Aura costs {1}{W}{W} = 3, plus {4} from two opposing Auras = 7 total
        harness.addMana(player2, "W", 6);

        // 6 mana is not enough (needs 3 + 4 = 7)
        assertThatThrownBy(() -> harness.castEnchantment(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Sacrifice ability =====

    @Test
    @DisplayName("Sacrificing Aura of Silence removes it from battlefield and puts it in graveyard")
    void sacrificeRemovesFromBattlefield() {
        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addToBattlefield(player2, new AngelicChorus());

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.sacrificePermanent(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Aura of Silence"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Aura of Silence"));
    }

    @Test
    @DisplayName("Sacrifice puts activated ability on the stack with target")
    void sacrificePutsAbilityOnStack() {
        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addToBattlefield(player2, new AngelicChorus());

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.sacrificePermanent(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Aura of Silence");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetPermanentId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Sacrifice ability resolves and destroys target enchantment")
    void sacrificeDestroysTargetEnchantment() {
        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addToBattlefield(player2, new AngelicChorus());

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.sacrificePermanent(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
    }

    @Test
    @DisplayName("Can sacrifice to destroy own enchantment")
    void canDestroyOwnEnchantment() {
        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addToBattlefield(player1, new AngelicChorus());

        UUID targetId = harness.getPermanentId(player1, "Angelic Chorus");
        harness.sacrificePermanent(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
    }

    @Test
    @DisplayName("Sacrifice ability fizzles if target is removed before resolution")
    void sacrificeFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addToBattlefield(player2, new AngelicChorus());

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.sacrificePermanent(player1, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot sacrifice targeting a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.sacrificePermanent(player1, 0, bearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target type");
    }

    @Test
    @DisplayName("Cannot sacrifice without specifying a target")
    void cannotSacrificeWithoutTarget() {
        harness.addToBattlefield(player1, new AuraOfSilence());

        assertThatThrownBy(() -> harness.sacrificePermanent(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires a target");
    }

    @Test
    @DisplayName("Cannot sacrifice a permanent that has no sacrifice abilities")
    void cannotSacrificePermanentWithoutAbility() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.addToBattlefield(player2, new AngelicChorus());

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");

        assertThatThrownBy(() -> harness.sacrificePermanent(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no sacrifice abilities");
    }

    @Test
    @DisplayName("Cost increase stops applying after Aura of Silence is sacrificed")
    void costIncreaseStopsAfterSacrifice() {
        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addToBattlefield(player2, new AngelicChorus());

        // Sacrifice the Aura
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.sacrificePermanent(player1, 0, targetId);
        harness.passBothPriorities();

        // Now player2 should be able to cast enchantments at normal cost
        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AuraOfSilence()));
        harness.addMana(player2, "W", 3);

        // 3 mana is enough (no more cost increase)
        harness.castEnchantment(player2, 0);

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getCard().getName()).isEqualTo("Aura of Silence");
    }
}
