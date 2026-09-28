package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ActivatedSleeper.class, GrizzlyBears.class, Shock.class})
class ActivatedSleeperTest extends BaseCardTest {

    @Test
    void copiesCreaturePutIntoGraveyardFromBattlefieldThisTurnAndAddsPhyrexian() {
        GrizzlyBears diedThisTurn = new GrizzlyBears();
        GrizzlyBears notFromBattlefield = new GrizzlyBears();
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, diedThisTurn);
        harness.setGraveyard(player2, List.of(notFromBattlefield));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, permanent.getId());

        ActivatedSleeper sleeperCard = new ActivatedSleeper();
        harness.setHand(player1, List.of(sleeperCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(diedThisTurn.getId());

        harness.handleMultipleCardsChosen(player1, List.of(diedThisTurn.getId()));

        Permanent sleeper = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getId().equals(sleeperCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(sleeper.getCard().getPower()).isEqualTo(2);
        assertThat(sleeper.getCard().getToughness()).isEqualTo(2);
        assertThat(sleeper.getCard().getSubtypes()).contains(CardSubtype.BEAR, CardSubtype.PHYREXIAN);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(diedThisTurn);
    }

    @Test
    void doesNotOfferCreatureThatEnteredTheGraveyardAnotherWay() {
        GrizzlyBears notFromBattlefield = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(notFromBattlefield));
        ActivatedSleeper sleeper = new ActivatedSleeper();
        harness.setHand(player1, List.of(sleeper));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sleeper);
    }
}
