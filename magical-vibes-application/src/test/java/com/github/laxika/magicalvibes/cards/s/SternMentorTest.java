package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SternMentorTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SternMentor()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private void activateMill(Permanent permanent) {
        permanent.setSummoningSick(false);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
        harness.activateAbility(player1, index, 0, null, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("While paired, Stern Mentor's granted ability mills the target player two cards")
    void pairedMentorMillsTwo() {
        castAndPairWithBears();
        Permanent mentor = findPermanent(player1, "Stern Mentor");

        List<Card> deck = gd.playerDecks.get(player2.getId());
        int sizeBefore = deck.size();
        List<Card> topTwo = List.copyOf(deck.subList(0, 2));

        activateMill(mentor);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(sizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(topTwo);
        assertThat(findPermanent(player1, "Stern Mentor").isTapped()).isTrue();
    }

    @Test
    @DisplayName("While paired, the partner also has the mill ability")
    void pairedPartnerMillsTwo() {
        Permanent bears = castAndPairWithBears();

        int sizeBefore = gd.playerDecks.get(player2.getId()).size();

        activateMill(bears);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(sizeBefore - 2);
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Unpaired Stern Mentor does not have the mill ability")
    void unpairedHasNoMillAbility() {
        harness.addToBattlefield(player1, new SternMentor());
        Permanent mentor = findPermanent(player1, "Stern Mentor");

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mentor);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
