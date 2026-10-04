package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FungalPlots.class, LlanowarElves.class, BalothGorger.class, Opt.class})
class FungalPlotsTest extends BaseCardTest {

    // =====================================================
    // Card properties
    // =====================================================
    // Ability 0: Create Saproling token
    // =====================================================

    @Test
    @DisplayName("Token ability prompts for graveyard exile cost choice")
    void tokenAbilityPromptsForGraveyardExileCost() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
    }

    @Test
    @DisplayName("Creature card is exiled from graveyard as cost")
    void creatureCardExiledFromGraveyard() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Mana is consumed when activating token ability")
    void manaIsConsumedForTokenAbility() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        // 3 total - 2 ({1}{G}) = 1 remaining
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolving token ability creates a 1/1 green Saproling creature token")
    void resolvingCreatesSaprolingToken() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = findPermanent(player1, "Saproling");
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
    }

    @Test
    @DisplayName("Cannot activate token ability without creature card in graveyard")
    void cannotActivateTokenAbilityWithoutCreatureInGraveyard() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot activate token ability with only non-creature cards in graveyard")
    void cannotActivateTokenAbilityWithOnlyNonCreatureInGraveyard() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.setGraveyard(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot activate token ability without enough mana")
    void cannotActivateTokenAbilityWithoutEnoughMana() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Can activate token ability multiple times with enough resources")
    void canActivateTokenAbilityMultipleTimes() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.setGraveyard(player1, List.of(new LlanowarElves(), new BalothGorger()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // First activation
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        // Second activation
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        long saprolingCount = countPermanents(player1, "Saproling");
        assertThat(saprolingCount).isEqualTo(2);
    }

    // =====================================================
    // Ability 1: Sacrifice two Saprolings
    // =====================================================

    @Test
    @DisplayName("Auto-sacrifices when exactly 2 Saprolings available")
    void autoSacrificesWhenExactlyTwoSaprolings() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.addToBattlefield(player1, createSaprolingToken());
        harness.addToBattlefield(player1, createSaprolingToken());

        harness.activateAbility(player1, 0, 1, null, null);

        // Both Saprolings should be auto-sacrificed
        assertThat(countPermanents(player1, "Saproling")).isZero();

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Prompts for Saproling choice when more than 2 available")
    void promptsForChoiceWhenMoreThanTwoSaprolings() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.addToBattlefield(player1, createSaprolingToken());
        harness.addToBattlefield(player1, createSaprolingToken());
        harness.addToBattlefield(player1, createSaprolingToken());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Completing two sacrifice choices puts ability on stack")
    void completingTwoSacrificesPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new FungalPlots());
        UUID sap1Id = harness.addToBattlefieldAndReturn(player1, createSaprolingToken()).getId();
        UUID sap2Id = harness.addToBattlefieldAndReturn(player1, createSaprolingToken()).getId();
        harness.addToBattlefield(player1, createSaprolingToken());

        harness.activateAbility(player1, 0, 1, null, null);

        // First choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sap1Id);

        // Second choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sap2Id);

        // Ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);

        // Sacrificed Saprolings should be gone
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolving sacrifice ability gains 2 life")
    void resolvingSacrificeAbilityGains2Life() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.addToBattlefield(player1, createSaprolingToken());
        harness.addToBattlefield(player1, createSaprolingToken());

        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("Resolving sacrifice ability draws a card")
    void resolvingSacrificeAbilityDrawsCard() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.addToBattlefield(player1, createSaprolingToken());
        harness.addToBattlefield(player1, createSaprolingToken());

        int startingHandSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(startingHandSize + 1);
    }

    @Test
    @DisplayName("Cannot activate sacrifice ability without 2 Saprolings")
    void cannotActivateSacrificeAbilityWithoutTwoSaprolings() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.addToBattlefield(player1, createSaprolingToken());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Cannot activate sacrifice ability with no Saprolings")
    void cannotActivateSacrificeAbilityWithNoSaprolings() {
        harness.addToBattlefield(player1, new FungalPlots());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Non-Saproling creatures do not count for sacrifice cost")
    void nonSaprolingCreaturesDoNotCountForSacrifice() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.addToBattlefield(player1, createSaprolingToken());
        harness.addToBattlefield(player1, new BalothGorger());

        // Only 1 Saproling + 1 non-Saproling creature = not enough
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Sacrifice ability does not require mana")
    void sacrificeAbilityDoesNotRequireMana() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.addToBattlefield(player1, createSaprolingToken());
        harness.addToBattlefield(player1, createSaprolingToken());

        // No mana added — should still work
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    // =====================================================
    // Integration: both abilities together
    // =====================================================

    @Test
    @DisplayName("Can create Saprolings then sacrifice them")
    void canCreateSaprolingsThenSacrificeThem() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.setGraveyard(player1, List.of(new LlanowarElves(), new BalothGorger()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Create first Saproling
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        // Create second Saproling
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        int lifeBeforeSacrifice = gd.playerLifeTotals.get(player1.getId());
        int handSizeBeforeSacrifice = gd.playerHands.get(player1.getId()).size();

        // Sacrifice both Saprolings (exactly 2 = auto-sacrifice)
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBeforeSacrifice + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeSacrifice + 1);
        assertThat(countPermanents(player1, "Saproling")).isZero();
    }

    // =====================================================
    // Helpers
    // =====================================================

    @Test
    @DisplayName("Opponent's graveyard cannot pay the token ability cost")
    void cannotExileCreatureFromOpponentsGraveyard() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Token ability requires green mana even with enough total mana")
    void tokenAbilityRequiresGreenMana() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiling a creature from a mixed graveyard creates a token only on resolution")
    void mixedGraveyardExileIsPaidBeforeTokenCreation() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.setGraveyard(player1, List.of(new Opt(), new BalothGorger()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertInGraveyard(player1, "Opt");
        harness.assertNotInGraveyard(player1, "Baloth Gorger");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Baloth Gorger"));
        assertThat(countPermanents(player1, "Saproling")).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Saproling")).isEqualTo(1);
        assertThat(countPermanents(player2, "Saproling")).isZero();
    }

    @Test
    @DisplayName("Opponent's Saprolings cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsSaprolings() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.addToBattlefield(player2, createSaprolingToken());
        harness.addToBattlefield(player2, createSaprolingToken());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        assertThat(countPermanents(player2, "Saproling")).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice cost is paid before life gain and card draw")
    void sacrificeCostIsPaidBeforeResolution() {
        harness.addToBattlefield(player1, new FungalPlots());
        harness.setGraveyard(player1, List.of(new LlanowarElves(), new BalothGorger()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, 0, null, null);
            harness.handleGraveyardCardChosen(player1, 0);
            harness.passBothPriorities();
        }
        harness.setLibrary(player1, List.of(new Opt()));
        int startingHandSize = gd.playerHands.get(player1.getId()).size();
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(countPermanents(player1, "Saproling")).isZero();
        harness.assertLife(player1, startingLife);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(startingHandSize);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, startingLife + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(startingHandSize + 1);
        harness.assertInHand(player1, "Opt");
        harness.assertLife(player2, opponentLife);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        harness.assertNotInGraveyard(player1, "Saproling");
    }

    private Card createSaprolingToken() {
        Card card = new Card();
        card.setName("Saproling");
        card.setType(CardType.CREATURE);
        card.setManaCost("{0}");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.SAPROLING));
        return card;
    }
}
