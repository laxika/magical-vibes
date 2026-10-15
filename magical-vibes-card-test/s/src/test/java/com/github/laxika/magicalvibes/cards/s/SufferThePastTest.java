package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SufferThePast.class, NestInvader.class, WitchbaneOrb.class, GroundSeal.class})
class SufferThePastTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=2 chooses exactly two cards from the target player's graveyard")
    void castingWithXPromptsForExactTargetCount() {
        Card card1 = new NestInvader();
        Card card2 = new NestInvader();
        harness.setGraveyard(player2, List.of(card1, card2));
        harness.setHand(player1, List.of(new SufferThePast()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, 2, player2.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactly(card1.getId(), card2.getId());
    }

    @Test
    @DisplayName("Exiling cards makes the target player lose life and the caster gain life")
    void exilingCardsDrainsTargetPlayer() {
        Card card1 = new NestInvader();
        Card card2 = new NestInvader();
        harness.setGraveyard(player2, List.of(card1, card2));
        harness.setHand(player1, List.of(new SufferThePast()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(card1.getId(), card2.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card1, card2);
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Cards that leave the graveyard before resolution do not count")
    void onlyCardsStillInTargetGraveyardCount() {
        Card card1 = new NestInvader();
        Card card2 = new NestInvader();
        harness.setGraveyard(player2, List.of(card1, card2));
        harness.setHand(player1, List.of(new SufferThePast()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(card1.getId(), card2.getId()));
        gd.playerGraveyards.get(player2.getId()).remove(card1);
        gd.playerHands.get(player2.getId()).add(card1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card2);
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("An illegal target player still allows exile and life gain, but not life loss")
    void illegalTargetPlayerStillAllowsExileAndLifeGain() {
        Card card = new NestInvader();
        harness.setGraveyard(player2, List.of(card));
        harness.setHand(player1, List.of(new SufferThePast()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.addToBattlefield(player2, new WitchbaneOrb());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("X=0 resolves without choosing graveyard cards")
    void xZeroDoesNothing() {
        harness.setHand(player1, List.of(new SufferThePast()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Casting with too few cards in the target player's graveyard is rejected")
    void insufficientCardsThrows() {
        harness.setGraveyard(player2, List.of(new NestInvader()));
        harness.setHand(player1, List.of(new SufferThePast()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough cards in target player's graveyard");
    }

    @Test
    @DisplayName("Cards made untargetable before resolution are not exiled or counted")
    void graveyardTargetsBecomingUntargetableAreNotExiled() {
        Card card = new NestInvader();
        harness.setGraveyard(player2, List.of(card));
        harness.setHand(player1, List.of(new SufferThePast()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.addToBattlefield(player2, new GroundSeal());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Ground Seal prevents choosing graveyard targets while casting")
    void untargetableGraveyardCardsCannotBeChosen() {
        harness.setGraveyard(player2, List.of(new NestInvader()));
        harness.addToBattlefield(player2, new GroundSeal());
        harness.setHand(player1, List.of(new SufferThePast()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough cards in target player's graveyard");
    }

    @Test
    @DisplayName("Targeting your own graveyard can exile noncreature cards with no net life change")
    void canExileNoncreatureFromOwnGraveyard() {
        Card graveyardCard = new SufferThePast();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new SufferThePast()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, 1, player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardCard);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("No life changes occur when all selected cards have left the graveyard")
    void noCardsExiledMeansNoLifeChanges() {
        Card card = new NestInvader();
        harness.setGraveyard(player2, List.of(card));
        harness.setHand(player1, List.of(new SufferThePast()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(card));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(card);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
