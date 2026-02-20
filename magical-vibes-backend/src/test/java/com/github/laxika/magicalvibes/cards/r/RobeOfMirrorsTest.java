package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RobeOfMirrorsTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameQueryService gqs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Robe of Mirrors has correct card properties")
    void hasCorrectProperties() {
        RobeOfMirrors card = new RobeOfMirrors();

        assertThat(card.getName()).isEqualTo("Robe of Mirrors");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.AURA);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(GrantKeywordToEnchantedCreatureEffect.class);
        GrantKeywordToEnchantedCreatureEffect effect =
                (GrantKeywordToEnchantedCreatureEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.keyword()).isEqualTo(Keyword.SHROUD);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Robe of Mirrors puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new RobeOfMirrors()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Robe of Mirrors");
    }

    @Test
    @DisplayName("Resolving Robe of Mirrors attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new RobeOfMirrors()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Robe of Mirrors")
                        && p.getAttachedTo() != null
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Shroud grants targeting protection =====

    @Test
    @DisplayName("Enchanted creature cannot be targeted by controller's spells (shroud blocks all targeting)")
    void enchantedCreatureCannotBeTargetedByController() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        // Attach Robe of Mirrors directly
        Permanent robePerm = new Permanent(new RobeOfMirrors());
        robePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(robePerm);

        // Controller tries to target own creature with Boomerang
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Enchanted creature cannot be targeted by opponent's spells")
    void enchantedCreatureCannotBeTargetedByOpponent() {
        // Player2 owns the creature with Robe, and is active player
        harness.forceActivePlayer(player2);
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bearsPerm);

        // Attach Robe of Mirrors directly
        Permanent robePerm = new Permanent(new RobeOfMirrors());
        robePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(robePerm);

        // Player2 passes priority so player1 gets it
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passPriority(player2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Enchanted creature cannot be targeted by controller's auras")
    void enchantedCreatureCannotBeTargetedByAuras() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        // Attach Robe of Mirrors directly
        Permanent robePerm = new Permanent(new RobeOfMirrors());
        robePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(robePerm);

        // Controller tries to target the creature with Pacifism
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    // ===== Shroud removed when Robe is removed =====

    @Test
    @DisplayName("Creature can be targeted again after Robe of Mirrors is removed")
    void creatureCanBeTargetedAfterRobeRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        // Attach Robe of Mirrors
        Permanent robePerm = new Permanent(new RobeOfMirrors());
        robePerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(robePerm);

        // Verify creature has shroud
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.SHROUD)).isTrue();

        // Remove the Robe
        gd.playerBattlefields.get(player1.getId()).remove(robePerm);

        // Verify creature no longer has shroud
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.SHROUD)).isFalse();

        // Now creature can be targeted — cast Boomerang on it
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Boomerang");
    }

    // ===== Fizzles if target removed =====

    @Test
    @DisplayName("Robe of Mirrors fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new RobeOfMirrors()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        // Remove the target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Robe of Mirrors"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Robe of Mirrors"));
    }

    // ===== Can be cast on own creature =====

    @Test
    @DisplayName("Robe of Mirrors can be cast on own creature")
    void canCastOnOwnCreature() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new RobeOfMirrors()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Robe of Mirrors")
                        && p.getAttachedTo() != null
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }
}

