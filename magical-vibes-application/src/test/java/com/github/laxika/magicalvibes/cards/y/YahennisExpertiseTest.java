package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YahennisExpertiseTest extends BaseCardTest {

    @Test
    @DisplayName("Gives all creatures -3/-3 until end of turn")
    void weakensAllCreaturesUntilEndOfTurn() {
        Permanent ownAngel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent opposingAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castExpertise(List.of(new YahennisExpertise()));

        assertThat(ownAngel.getEffectivePower()).isEqualTo(1);
        assertThat(ownAngel.getEffectiveToughness()).isEqualTo(1);
        assertThat(opposingAngel.getEffectivePower()).isEqualTo(1);
        assertThat(opposingAngel.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownAngel.getEffectivePower()).isEqualTo(4);
        assertThat(ownAngel.getEffectiveToughness()).isEqualTo(4);
        assertThat(opposingAngel.getEffectivePower()).isEqualTo(4);
        assertThat(opposingAngel.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Offers a spell with mana value three or less from hand for free")
    void castsLowManaValueSpellFromHand() {
        GrizzlyBears bears = new GrizzlyBears();
        castExpertise(List.of(new YahennisExpertise(), bears));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Does not offer a spell with mana value greater than three")
    void doesNotOfferHighManaValueSpell() {
        castExpertise(List.of(new YahennisExpertise(), new SerraAngel()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castExpertise(List<Card> hand) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
    }
}
