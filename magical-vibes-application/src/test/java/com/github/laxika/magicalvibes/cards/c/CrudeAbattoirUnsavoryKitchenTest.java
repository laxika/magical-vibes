package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.RED;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrudeAbattoirUnsavoryKitchen.class, GrizzlyBears.class})
class CrudeAbattoirUnsavoryKitchenTest extends BaseCardTest {

    @Test
    void crudeAbattoirDealsTwoDamageToTheChosenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature("Target", 5));

        castRoom(0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void unsavoryKitchenPerpetuallyBoostsAChosenCreatureCardInHand() {
        castRoom(1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature("Target", 5));
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new CrudeAbattoirUnsavoryKitchen(), bears));
        harness.addMana(player1, RED, 1);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualCreatureCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Card modified = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(modified.getPower()).isEqualTo(4);
        assertThat(modified.getKeywords()).contains(Keyword.HASTE);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    private void castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new CrudeAbattoirUnsavoryKitchen()));
        harness.addMana(player1, RED, doorIndex == 0 ? 1 : 3);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
    }

    private Card creature(String name, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}{R}");
        card.setPower(3);
        card.setToughness(toughness);
        return card;
    }
}
