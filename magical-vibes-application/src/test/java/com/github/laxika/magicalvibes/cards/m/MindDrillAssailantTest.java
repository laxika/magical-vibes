package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindDrillAssailant.class, GrizzlyBears.class})
class MindDrillAssailantTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+0 with seven cards in its controller's graveyard")
    void thresholdBonusAtSevenCards() {
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent assailant = harness.addToBattlefieldAndReturn(player1, new MindDrillAssailant());

        assertThat(gqs.getEffectivePower(gd, assailant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, assailant)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not get its threshold bonus below seven cards")
    void noThresholdBonusBelowSevenCards() {
        harness.setGraveyard(player1, graveyardCards(6));
        Permanent assailant = harness.addToBattlefieldAndReturn(player1, new MindDrillAssailant());

        assertThat(gqs.getEffectivePower(gd, assailant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, assailant)).isEqualTo(5);
    }

    @Test
    @DisplayName("Pays for the activated ability and surveils 1")
    void activatedAbilitySurveilsOne() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MindDrillAssailant());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
