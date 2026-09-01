package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({MuseVessel.class, GrizzlyBears.class, Forest.class})
class MuseVesselTest extends BaseCardTest {

    @Test
    void targetPlayerExilesCardFromHandAndTracksItWithVessel() {
        Permanent vessel = addVessel();
        Card exiled = new GrizzlyBears();
        harness.setHand(player2, List.of(exiled));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.getCardsExiledByPermanent(vessel.getId())).containsExactly(exiled);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(vessel.isTapped()).isTrue();
    }

    @Test
    void choosesOneExiledCardAndMayPlayItThisTurn() {
        Permanent vessel = addVessel();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        gd.addToExile(player1.getId(), creature, vessel.getId());
        gd.addToExile(player1.getId(), land, vessel.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.ExiledCardMayPlayChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ExiledCardMayPlayChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId(), land.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == creature);
        assertThat(gd.getCardsExiledByPermanent(vessel.getId())).containsExactly(land);
    }

    private Permanent addVessel() {
        return harness.addToBattlefieldAndReturn(player1, new MuseVessel());
    }
}
