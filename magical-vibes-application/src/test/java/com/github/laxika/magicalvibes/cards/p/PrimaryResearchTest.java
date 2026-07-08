package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.CardsLeftGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrimaryResearchTest extends BaseCardTest {

    private void castAndResolveSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PrimaryResearch()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment spell → ETB on stack
    }

    // ===== Structure =====

    @Test
    @DisplayName("ETB reanimation + gated end-step draw")
    void hasCorrectEffects() {
        PrimaryResearch card = new PrimaryResearch();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ReturnCardFromGraveyardEffect.class);

        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED)).hasSize(1);
        ConditionalEffect conditional =
                (ConditionalEffect) card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst();
        assertThat(conditional.condition()).isInstanceOf(CardsLeftGraveyardThisTurn.class);
        assertThat(conditional.wrapped()).isInstanceOf(DrawCardEffect.class);
    }

    // ===== ETB reanimation =====

    @Test
    @DisplayName("Returns a nonland permanent card with mana value 3 or less to the battlefield")
    void reanimatesLowManaValuePermanent() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears())); // 2/2 creature, MV 2
        castAndResolveSpell();

        harness.passBothPriorities(); // resolve ETB → graveyard choice prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A permanent card with mana value greater than 3 is not a valid choice")
    void cannotReanimateHighManaValue() {
        harness.setGraveyard(player1, List.of(new AvatarOfMight())); // MV 8
        castAndResolveSpell();
        harness.passBothPriorities();

        // No matching card → no graveyard prompt
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Avatar of Might"));
    }

    @Test
    @DisplayName("A land card is not a valid choice")
    void cannotReanimateLand() {
        harness.setGraveyard(player1, List.of(new Forest()));
        castAndResolveSpell();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    // ===== End-step draw =====

    @Test
    @DisplayName("Reanimating a card triggers the end-step draw (a card left your graveyard)")
    void reanimationTriggersEndStepDraw() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new Forest()));
        castAndResolveSpell();

        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0); // Grizzly Bears leaves the graveyard

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP → trigger fires
        harness.passBothPriorities(); // resolve the draw trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("No end-step draw when nothing left your graveyard this turn")
    void noEndStepDrawWithoutGraveyardExit() {
        harness.addToBattlefield(player1, new PrimaryResearch());
        setDeck(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP; no trigger should fire

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Cannot choose a high-mana-value card even when it sits in the graveyard alongside a legal one")
    void onlyLowManaValueCardsAreValidChoices() {
        harness.setGraveyard(player1, List.of(new AvatarOfMight(), new GrizzlyBears()));
        castAndResolveSpell();
        harness.passBothPriorities();

        // Index 0 is Avatar of Might (MV 8) — not a valid choice
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
