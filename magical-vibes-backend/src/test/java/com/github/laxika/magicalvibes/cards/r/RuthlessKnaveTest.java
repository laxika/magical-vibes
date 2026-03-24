package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuthlessKnaveTest extends BaseCardTest {

    // =====================================================
    // Card properties
    // =====================================================

    @Test
    @DisplayName("Ruthless Knave has correct activated abilities")
    void hasCorrectAbilities() {
        RuthlessKnave card = new RuthlessKnave();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // Ability 0: {2}{B}, Sacrifice a creature: Create two Treasure tokens.
        var treasureAbility = card.getActivatedAbilities().get(0);
        assertThat(treasureAbility.isRequiresTap()).isFalse();
        assertThat(treasureAbility.getManaCost()).isEqualTo("{2}{B}");
        assertThat(treasureAbility.getEffects()).hasSize(2);
        assertThat(treasureAbility.getEffects().get(0)).isInstanceOf(SacrificePermanentCost.class);
        assertThat(treasureAbility.getEffects().get(1)).isInstanceOf(CreateTokenEffect.class);
        CreateTokenEffect tokenEffect = (CreateTokenEffect) treasureAbility.getEffects().get(1);
        assertThat(tokenEffect.amount()).isEqualTo(2);

        // Ability 1: Sacrifice three Treasures: Draw a card.
        var drawAbility = card.getActivatedAbilities().get(1);
        assertThat(drawAbility.isRequiresTap()).isFalse();
        assertThat(drawAbility.getManaCost()).isNull();
        assertThat(drawAbility.getEffects()).hasSize(2);
        assertThat(drawAbility.getEffects().get(0)).isInstanceOf(SacrificeMultiplePermanentsCost.class);
        SacrificeMultiplePermanentsCost sacCost = (SacrificeMultiplePermanentsCost) drawAbility.getEffects().get(0);
        assertThat(sacCost.count()).isEqualTo(3);
        assertThat(drawAbility.getEffects().get(1)).isInstanceOf(DrawCardEffect.class);
    }

    // =====================================================
    // Ability 0: Sacrifice a creature, create two Treasures
    // =====================================================

    @Test
    @DisplayName("Auto-sacrifices when only one other creature available and creates two Treasures")
    void autoSacrificesOnlyCreatureAndCreatesTreasures() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Knave is excluded as source, so Llanowar Elves is the only valid target — auto-sacrifice
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        harness.assertInGraveyard(player1, "Llanowar Elves");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        List<Permanent> treasures = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .toList();
        assertThat(treasures).hasSize(2);
        for (Permanent treasure : treasures) {
            assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
            assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
        }
    }

    @Test
    @DisplayName("Prompts for creature choice when multiple other creatures available")
    void promptsForCreatureChoiceWhenMultipleAvailable() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Choosing a creature to sacrifice puts ability on stack")
    void choosingCreaturePutsAbilityOnStack() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID elvesId = findPermanent(player1, "Llanowar Elves").getId();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, elvesId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Mana is consumed when activating treasure ability")
    void manaIsConsumedForTreasureAbility() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        // 4 total - 3 ({2}{B}) = 1 remaining
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate treasure ability without enough mana")
    void cannotActivateTreasureAbilityWithoutEnoughMana() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Cannot activate treasure ability when Knave is the only creature")
    void cannotActivateTreasureAbilityWhenKnaveIsOnlyCreature() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Knave is excluded as source, so there are no valid sacrifice targets
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Sacrifice a creature");
    }

    @Test
    @DisplayName("Can activate treasure ability multiple times with enough resources")
    void canActivateTreasureAbilityMultipleTimes() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        // First activation — 2 valid targets, prompts for choice
        UUID elvesId = findPermanent(player1, "Llanowar Elves").getId();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, elvesId);
        harness.passBothPriorities();

        // Second activation — only Grizzly Bears left, auto-sacrifice
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        long treasureCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .count();
        assertThat(treasureCount).isEqualTo(4);
    }

    // =====================================================
    // Ability 1: Sacrifice three Treasures, draw a card
    // =====================================================

    @Test
    @DisplayName("Auto-sacrifices when exactly 3 Treasures available")
    void autoSacrificesWhenExactlyThreeTreasures() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .count()).isZero();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving draw ability draws a card")
    void resolvingDrawAbilityDrawsCard() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());

        int startingHandSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(startingHandSize + 1);
    }

    @Test
    @DisplayName("Prompts for Treasure choice when more than 3 available")
    void promptsForChoiceWhenMoreThanThreeTreasures() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Completing three sacrifice choices puts ability on stack")
    void completingThreeSacrificesPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());

        UUID t1Id = gd.playerBattlefields.get(player1.getId()).get(1).getId();
        UUID t2Id = gd.playerBattlefields.get(player1.getId()).get(2).getId();
        UUID t3Id = gd.playerBattlefields.get(player1.getId()).get(3).getId();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        harness.handlePermanentChosen(player1, t1Id);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        harness.handlePermanentChosen(player1, t2Id);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        harness.handlePermanentChosen(player1, t3Id);

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate draw ability without 3 Treasures")
    void cannotActivateDrawAbilityWithoutThreeTreasures() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Cannot activate draw ability with no Treasures")
    void cannotActivateDrawAbilityWithNoTreasures() {
        harness.addToBattlefield(player1, new RuthlessKnave());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Draw ability does not require mana")
    void drawAbilityDoesNotRequireMana() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());

        // No mana added — should still work
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // =====================================================
    // Integration: both abilities together
    // =====================================================

    @Test
    @DisplayName("Can sacrifice a creature to create Treasures then sacrifice Treasures to draw")
    void canCreateTreasuresThenSacrificeThemToDraw() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Sacrifice Llanowar Elves (only valid creature) to create 2 Treasures → 3 total
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        long treasureCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .count();
        assertThat(treasureCount).isEqualTo(3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Sacrifice 3 Treasures to draw a card (exactly 3 = auto-sacrifice)
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .count()).isZero();
    }

    // =====================================================
    // Helpers
    // =====================================================

    private Card createTreasureToken() {
        Card card = new Card();
        card.setName("Treasure");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{0}");
        card.setSubtypes(List.of(CardSubtype.TREASURE));
        return card;
    }

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(name + " not found"));
    }
}
