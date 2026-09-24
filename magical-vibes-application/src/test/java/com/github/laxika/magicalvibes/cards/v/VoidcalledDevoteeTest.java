package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CantorOfTheRefrain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoidcalledDevotee.class, CantorOfTheRefrain.class})
class VoidcalledDevoteeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking conjures Cantor of the Refrain into its controller's graveyard")
    void attackingConjuresCantorIntoGraveyard() {
        Permanent devotee = addCreatureReady(player1, new VoidcalledDevotee());

        declareAttackers(java.util.List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Cantor of the Refrain"));
        assertThat(devotee.isAttackedThisTurn()).isTrue();
    }
}
