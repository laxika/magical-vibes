package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VaevictisAsmadiTheDireTest extends BaseCardTest {

    @Test
    void sacrificesOnePermanentPerPlayerAndPutsPermanentTopCardsOntoTheBattlefield() {
        Permanent vaevictis = addCreatureReady(player1, new VaevictisAsmadiTheDire());
        Permanent ownTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new AirElemental()));
        Card nonPermanent = new Shock();
        harness.setLibrary(player2, List.of(nonPermanent));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, ownTarget.getId());
        harness.handlePermanentChosen(player1, opponentTarget.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(ownTarget.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Air Elemental"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentTarget.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nonPermanent);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(vaevictis);
    }
}
