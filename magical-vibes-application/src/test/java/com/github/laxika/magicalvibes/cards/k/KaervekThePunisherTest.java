package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TragicSlip;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaervekThePunisher.class, BogRats.class, GrizzlyBears.class, Shock.class, TragicSlip.class})
class KaervekThePunisherTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and copies a targeted black card, then casts the copy for its normal cost")
    void copiesAndCastsBlackCard() {
        harness.addToBattlefield(player1, new KaervekThePunisher());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card graveyardCard = new TragicSlip();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        int lifeBeforeCopy = gd.getLife(player1.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ExileCastSpellTarget.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBeforeCopy - 2);
        assertThat(creature.getPowerModifier()).isEqualTo(-1);
        assertThat(creature.getToughnessModifier()).isEqualTo(-1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCard.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Declining the copy leaves the exiled card alone and causes no life loss")
    void declinesCopy() {
        harness.addToBattlefield(player1, new KaervekThePunisher());
        Card graveyardCard = new TragicSlip();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCard.getId()));
    }

    @Test
    @DisplayName("A copied permanent spell enters as a token")
    void copiedPermanentBecomesToken() {
        harness.addToBattlefield(player1, new KaervekThePunisher());
        Card graveyardCard = new BogRats();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Can target only a black card in the controller's graveyard")
    void targetsOnlyOwnBlackCards() {
        harness.addToBattlefield(player1, new KaervekThePunisher());
        Card opponentCard = new TragicSlip();
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
    }
}
