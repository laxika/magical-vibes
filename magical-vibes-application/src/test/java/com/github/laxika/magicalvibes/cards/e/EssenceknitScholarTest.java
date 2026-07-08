package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.CreatureDiedUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EssenceknitScholarTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("ETB creates a Pest token with an attack-gain-life trigger")
    void hasEtbPestTokenStructure() {
        EssenceknitScholar card = new EssenceknitScholar();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(CreateTokenEffect.class);

        CreateTokenEffect token = (CreateTokenEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(token.tokenName()).isEqualTo("Pest");
        assertThat(token.colors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(token.subtypes()).contains(CardSubtype.PEST);
        assertThat(token.tokenEffects().get(EffectSlot.ON_ATTACK)).isEqualTo(new GainLifeEffect(1));
    }

    @Test
    @DisplayName("End-step trigger is gated on a creature dying under your control")
    void hasEndStepStructure() {
        EssenceknitScholar card = new EssenceknitScholar();

        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED)).hasSize(1);
        ConditionalEffect conditional =
                (ConditionalEffect) card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst();
        assertThat(conditional.condition()).isInstanceOf(CreatureDiedUnderYourControlThisTurn.class);
        assertThat(conditional.wrapped()).isInstanceOf(DrawCardEffect.class);
    }

    // ===== ETB Pest token =====

    @Test
    @DisplayName("Casting Essenceknit Scholar creates a 1/1 Pest token")
    void etbCreatesPestToken() {
        harness.setHand(player1, List.of(new EssenceknitScholar()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent pest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Pest"))
                .findFirst().orElse(null);
        assertThat(pest).isNotNull();
    }

    // ===== End-step conditional draw =====

    @Test
    @DisplayName("Draws a card if a creature died under your control this turn")
    void drawsWhenCreatureDiedUnderYourControl() {
        harness.addToBattlefield(player1, new EssenceknitScholar());
        setDeck(player1, List.of(new Forest()));
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);
        // Trigger is on the stack; resolve it
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Does not draw when only an opponent's creature died this turn")
    void noDrawWhenOnlyOpponentCreatureDied() {
        harness.addToBattlefield(player1, new EssenceknitScholar());
        setDeck(player1, List.of(new Forest()));
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1); // opponent's death only

        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    // ===== Helpers =====

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance POSTCOMBAT_MAIN → END_STEP, triggers fire
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
