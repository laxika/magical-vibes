package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConsumedByHistory.class, GrizzlyBears.class})
class ConsumedByHistoryTest extends BaseCardTest {

    @Test
    void dealsThreeDamageToEachCreatureAndPerpetuallyGrantsUnearthToNontokenDeaths() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConsumedByHistory()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player2);
        int graveyardIndex = gd.playerGraveyards.get(player2.getId()).indexOf(bears.getCard());
        harness.activateGraveyardAbility(player2, graveyardIndex);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(bears.getCard().getId()));
    }
}
