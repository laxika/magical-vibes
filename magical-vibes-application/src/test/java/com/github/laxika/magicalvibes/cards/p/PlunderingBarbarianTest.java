package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlunderingBarbarian.class, MindStone.class})
class PlunderingBarbarianTest extends BaseCardTest {

    @Test
    void smashTheChestDestroysTargetArtifact() {
        Permanent mindStone = harness.addToBattlefieldAndReturn(player2, new MindStone());
        castBarbarian();

        harness.handleListChoice(player1, "Smash the Chest \u2014 Destroy target artifact.");
        harness.handlePermanentChosen(player1, mindStone.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mind Stone");
    }

    @Test
    void pryItOpenCreatesTreasureToken() {
        castBarbarian();

        harness.handleListChoice(player1, "Pry It Open \u2014 Create a Treasure token.");
        harness.passBothPriorities();

        List<Permanent> treasures = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .toList();
        assertThat(treasures).hasSize(1);
    }

    private void castBarbarian() {
        harness.setHand(player1, List.of(new PlunderingBarbarian()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
