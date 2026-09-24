package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HollowhengeWrangler.class, HollowhengeBeast.class, Forest.class})
class HollowhengeWranglerTest extends BaseCardTest {

    @Test
    void seeksALandWhenItEntersTheBattlefield() {
        Card nonland = new HollowhengeBeast();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(nonland, land));

        harness.enterBattlefieldAndReturn(player1, new HollowhengeWrangler());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
    }

    @Test
    void discardingALandConjuresAHollowhengeBeastFromTheBattlefield() {
        harness.addToBattlefield(player1, new HollowhengeWrangler());
        Forest land = new Forest();
        harness.setHand(player1, List.of(land));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Hollowhenge Beast"));
    }

    @Test
    void discardingALandConjuresAHollowhengeBeastFromTheGraveyard() {
        HollowhengeWrangler wrangler = new HollowhengeWrangler();
        harness.setGraveyard(player1, List.of(wrangler));
        Forest land = new Forest();
        harness.setHand(player1, List.of(land));

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(wrangler.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Hollowhenge Beast"));
    }
}
