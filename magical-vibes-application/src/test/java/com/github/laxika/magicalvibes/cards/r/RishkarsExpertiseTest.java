package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.d.Disallow;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RishkarsExpertise.class, ColossalDreadmaw.class, Disallow.class,
        GrizzlyBears.class, HillGiant.class, SerraAngel.class})
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

    @Test
    @DisplayName("A drawn card can be cast for free after the draw completes")
    void castsCardDrawnByExpertise() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        SerraAngel angel = new SerraAngel();
        ColossalDreadmaw dreadmaw = new ColossalDreadmaw();
        harness.setLibrary(player1, List.of(angel, dreadmaw));
        castExpertise(List.of(new RishkarsExpertise()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(angel, dreadmaw);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(dreadmaw);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(angel.getId()));
    }

    @Test
    @DisplayName("Declining the free cast leaves the drawn cards in hand")
    void mayDeclineFreeCastAfterDrawing() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        SerraAngel angel = new SerraAngel();
        ColossalDreadmaw dreadmaw = new ColossalDreadmaw();
        harness.setLibrary(player1, List.of(angel, dreadmaw));
        castExpertise(List.of(new RishkarsExpertise()));

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(angel, dreadmaw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Without creatures Expertise draws nothing but still allows a free cast")
    void noCreaturesDoesNotPreventFreeCast() {
        SerraAngel angel = new SerraAngel();
        ColossalDreadmaw libraryCard = new ColossalDreadmaw();
        harness.setLibrary(player1, List.of(libraryCard));
        castExpertise(List.of(new RishkarsExpertise(), angel));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(angel);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(angel.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("The free spell can target the resolving Expertise spell")
    void freeCounterspellCanTargetResolvingExpertise() {
        RishkarsExpertise expertise = new RishkarsExpertise();
        Disallow disallow = new Disallow();
        castExpertise(List.of(expertise, disallow));

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, expertise.getId());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(disallow);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(expertise);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(disallow);
        assertThat(gd.stack).isEmpty();
    }

    private void castExpertise(List<Card> hand) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, List.of());
    }
}
