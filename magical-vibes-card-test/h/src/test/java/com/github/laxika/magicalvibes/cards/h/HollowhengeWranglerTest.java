package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.assertInHand(player1, "Hollowhenge Beast");
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
        harness.assertInHand(player1, "Hollowhenge Beast");
    }

    @Test
    void seekingWithoutALandLeavesTheLibraryUnchanged() {
        Card first = new HollowhengeBeast();
        Card second = new HollowhengeWrangler();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));

        harness.enterBattlefieldAndReturn(player1, new HollowhengeWrangler());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void cannotActivateOnTheBattlefieldWithoutALandToDiscard() {
        harness.addToBattlefield(player1, new HollowhengeWrangler());
        Card nonland = new HollowhengeBeast();
        harness.setHand(player1, List.of(nonland));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateInTheGraveyardWithoutALandToDiscard() {
        HollowhengeWrangler wrangler = new HollowhengeWrangler();
        harness.setGraveyard(player1, List.of(wrangler));
        Card nonland = new HollowhengeBeast();
        harness.setHand(player1, List.of(nonland));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(wrangler);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateRepeatedlyFromTheGraveyardAndPaysBeforeResolution() {
        HollowhengeWrangler wrangler = new HollowhengeWrangler();
        harness.setGraveyard(player1, List.of(wrangler));
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        harness.setHand(player1, List.of(firstLand, secondLand));

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondLand);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(wrangler, firstLand);
        harness.passBothPriorities();

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(wrangler, firstLand, secondLand);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2)
                .allMatch(card -> card instanceof HollowhengeBeast)
                .allMatch(card -> player1.getId().equals(card.getOwnerId()));
        assertThat(gd.playerHands.get(player1.getId()).get(0).getId())
                .isNotEqualTo(gd.playerHands.get(player1.getId()).get(1).getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(card -> card instanceof HollowhengeBeast);
    }
}
