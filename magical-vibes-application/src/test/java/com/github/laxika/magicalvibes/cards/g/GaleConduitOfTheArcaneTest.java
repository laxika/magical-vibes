package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaleConduitOfTheArcane.class, HolyDay.class, CounselOfTheSoratami.class,
        Mountain.class, Shock.class, GrizzlyBears.class})
class GaleConduitOfTheArcaneTest extends BaseCardTest {

    @Test
    void castReturnsAnInstantOrSorceryFromGraveyard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new GaleConduitOfTheArcane()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(instant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(instant.getId()));
    }

    @Test
    void castDoesNotReturnAcreatureFromGraveyard() {
        Card creature = new GaleConduitOfTheArcane();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new GaleConduitOfTheArcane()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    void stormFacePerpetuallyBoostsCreaturesAfterCastingAnInstant() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GaleConduitOfTheArcane(), new Mountain(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 3, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent gale = findPermanent(player1, "Gale, Storm Conduit");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gale)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(3);
    }
}
