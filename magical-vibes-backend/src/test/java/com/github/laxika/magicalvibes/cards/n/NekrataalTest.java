package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NekrataalTest {

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
    @DisplayName("Nekrataal has correct card properties")
    void hasCorrectProperties() {
        Nekrataal card = new Nekrataal();

        assertThat(card.getName()).isEqualTo("Nekrataal");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getKeywords()).containsExactly(Keyword.FIRST_STRIKE);
        assertThat(card.getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.ASSASSIN);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(DestroyTargetPermanentEffect.class);
        DestroyTargetPermanentEffect effect = (DestroyTargetPermanentEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.cannotBeRegenerated()).isTrue();
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Nekrataal puts it on the stack with target")
    void castingPutsItOnStackWithTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Nekrataal");
        assertThat(entry.getTargetPermanentId()).isEqualTo(targetId);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolving Nekrataal enters battlefield and triggers ETB destroy")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Nekrataal"));

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Nekrataal");
        assertThat(trigger.getTargetPermanentId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB resolves and destroys target creature")
    void etbDestroysTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Target restrictions =====

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new MassOfGhouls());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Mass of Ghouls");

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        harness.addToBattlefield(player2, new BottleGnomes());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Bottle Gnomes");

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonartifact");
    }

    // ===== Regeneration bypass =====

    @Test
    @DisplayName("Destroyed creature cannot be regenerated")
    void destroyedCreatureCannotBeRegenerated() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Give the target a regeneration shield before ETB resolves
        Permanent target = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        target.setRegenerationShield(1);

        // Resolve ETB — should destroy despite regeneration shield
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Indestructible =====

    @Test
    @DisplayName("Indestructible creature survives Nekrataal's ETB")
    void indestructibleCreatureSurvives() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Grant indestructible to the target before ETB resolves
        Permanent target = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        // Resolve ETB — should not destroy indestructible creature
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("indestructible"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== No target scenarios =====

    @Test
    @DisplayName("Can cast without a target when no valid creatures on battlefield")
    void canCastWithoutTargetWhenNoValidCreatures() {
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Nekrataal");
    }

    @Test
    @DisplayName("ETB does not trigger when cast without a target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Nekrataal"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can cast without target when only black creatures exist")
    void canCastWithoutTargetWhenOnlyBlackCreatures() {
        harness.addToBattlefield(player2, new MassOfGhouls());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Nekrataal");
    }

    // ===== Keywords =====

    @Test
    @DisplayName("Nekrataal has first strike keyword on the battlefield")
    void hasFirstStrike() {
        harness.addToBattlefield(player1, new Nekrataal());

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()).getFirst()
                .hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    // ===== Mana validation =====

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Nekrataal()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}

