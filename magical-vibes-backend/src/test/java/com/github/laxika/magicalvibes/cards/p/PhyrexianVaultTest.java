package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.CreatureYouControlTargetFilter;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhyrexianVaultTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Phyrexian Vault has correct card properties")
    void hasCorrectProperties() {
        PhyrexianVault card = new PhyrexianVault();

        assertThat(card.getName()).isEqualTo("Phyrexian Vault");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{3}");
        assertThat(card.getColor()).isNull();
        assertThat(card.getPower()).isNull();
        assertThat(card.getToughness()).isNull();
    }

    @Test
    @DisplayName("Phyrexian Vault has correct activated ability structure")
    void hasCorrectAbilityStructure() {
        PhyrexianVault card = new PhyrexianVault();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{2}");
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getTargetFilter()).isInstanceOf(CreatureYouControlTargetFilter.class);
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeCreatureCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(DrawCardEffect.class);

        DrawCardEffect draw = (DrawCardEffect) ability.getEffects().get(1);
        assertThat(draw.amount()).isEqualTo(1);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PhyrexianVault()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Phyrexian Vault");
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new PhyrexianVault()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phyrexian Vault"));
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new PhyrexianVault()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Activation: sacrificing a creature and drawing =====

    @Test
    @DisplayName("Activating ability sacrifices creature, taps vault, and puts draw on stack")
    void activatingAbilitySacrificesCreatureAndPutsDrawOnStack() {
        addReadyVault(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, bearsId);

        // Grizzly Bears should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Phyrexian Vault should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phyrexian Vault"));

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Phyrexian Vault");
    }

    @Test
    @DisplayName("Activating ability taps the vault")
    void activatingTapsVault() {
        Permanent vault = addReadyVault(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThat(vault.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, bearsId);

        assertThat(vault.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addReadyVault(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, bearsId);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving ability draws a card")
    void resolvingDrawsACard() {
        addReadyVault(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player1, List.of());
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Resolving ability does not affect opponent's hand")
    void doesNotAffectOpponent() {
        addReadyVault(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Vault remains on battlefield after activation and resolution")
    void remainsOnBattlefieldAfterResolution() {
        addReadyVault(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 2);
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phyrexian Vault"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Phyrexian Vault"));
    }

    @Test
    @DisplayName("Drawing from empty deck is handled")
    void drawingFromEmptyDeck() {
        addReadyVault(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player1, List.of());
        gd.playerDecks.get(player1.getId()).clear();

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards to draw"));
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyVault(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent vault = addReadyVault(player1);
        vault.tap();
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutSacrificeTarget() {
        addReadyVault(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose a creature to sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentCreature() {
        addReadyVault(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must sacrifice a creature you control");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-creature permanent")
    void cannotSacrificeNonCreature() {
        addReadyVault(player1);
        Card enchantment = new Card();
        enchantment.setName("Test Enchantment");
        enchantment.setType(CardType.ENCHANTMENT);
        enchantment.setManaCost("{1}{W}");
        enchantment.setColor(CardColor.WHITE);
        harness.addToBattlefield(player1, enchantment);
        UUID enchantmentId = harness.getPermanentId(player1, "Test Enchantment");
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantmentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must sacrifice a creature");
    }

    // ===== No summoning sickness for artifacts =====

    @Test
    @DisplayName("Can activate ability the turn it enters the battlefield (no summoning sickness for artifacts)")
    void noSummoningSicknessForArtifact() {
        PhyrexianVault card = new PhyrexianVault();
        Permanent vault = new Permanent(card);
        vault.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(vault);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, bearsId);

        assertThat(vault.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Logging =====

    @Test
    @DisplayName("Sacrificing a creature logs the sacrifice")
    void sacrificingCreatureLogsIt() {
        addReadyVault(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, bearsId);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("sacrifices Grizzly Bears"));
    }

    @Test
    @DisplayName("Activating ability logs the activation")
    void activatingAbilityLogsActivation() {
        addReadyVault(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, bearsId);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("activates Phyrexian Vault's ability"));
    }

    @Test
    @DisplayName("Resolving ability logs the card draw")
    void resolvingLogsCardDraw() {
        addReadyVault(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 2);
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("draws a card"));
    }

    // ===== Helpers =====

    private Permanent addReadyVault(Player player) {
        PhyrexianVault card = new PhyrexianVault();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setDeck(Player player, List<? extends Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}

