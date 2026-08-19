package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SramsExpertiseTest extends BaseCardTest {

    @Test
    @DisplayName("Cast creates three 1/1 colorless Servo artifact creature tokens")
    void createsThreeServoTokens() {
        castExpertise(List.of(new SramsExpertise()));

        assertThat(servos()).hasSize(3);
        assertThat(servos()).allSatisfy(servo -> {
            assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(servo.getCard().getSubtypes()).contains(CardSubtype.SERVO);
            assertThat(servo.getEffectivePower()).isEqualTo(1);
            assertThat(servo.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Cast offers a spell with mana value three or less from hand for free")
    void castsLowManaValueSpellFromHand() {
        GrizzlyBears bears = new GrizzlyBears();
        castExpertise(List.of(new SramsExpertise(), bears));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Cast does not offer a spell with mana value greater than three")
    void doesNotOfferHighManaValueSpell() {
        castExpertise(List.of(new SramsExpertise(), new SerraAngel()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castExpertise(List<Card> hand) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
    }

    private List<Permanent> servos() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .toList();
    }
}
