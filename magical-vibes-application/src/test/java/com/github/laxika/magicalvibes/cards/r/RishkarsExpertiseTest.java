package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RishkarsExpertiseTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards equal to the greatest power among creatures you control")
    void drawsCardsEqualToGreatestControlledCreaturePower() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        harness.setLibrary(player1, List.of(new SerraAngel(), new SerraAngel(), new SerraAngel(), new SerraAngel()));
        castExpertise(List.of(new RishkarsExpertise()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Offers a spell with mana value five or less from hand for free")
    void castsSpellWithManaValueAtMostFiveFromHand() {
        SerraAngel angel = new SerraAngel();
        castExpertise(List.of(new RishkarsExpertise(), angel));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(angel.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(angel.getId()));
    }

    @Test
    @DisplayName("Does not offer a spell with mana value greater than five")
    void doesNotOfferSpellWithManaValueGreaterThanFive() {
        ColossalDreadmaw dreadmaw = new ColossalDreadmaw();
        castExpertise(List.of(new RishkarsExpertise(), dreadmaw));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(dreadmaw);
        assertThat(gd.stack).isEmpty();
    }

    private void castExpertise(List<Card> hand) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
    }
}
