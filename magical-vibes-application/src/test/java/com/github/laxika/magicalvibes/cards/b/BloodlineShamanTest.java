package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodlineShaman.class, AvianChangeling.class, Forest.class, GrizzlyBears.class, WalkingCorpse.class})
class BloodlineShamanTest extends BaseCardTest {

    @Test
    @DisplayName("A creature card of the chosen type goes into its controller's hand")
    void matchingCreatureGoesToHand() {
        GrizzlyBears bear = new GrizzlyBears();
        activateAndChoose(bear, "BEAR");

        assertThat(gd.playerHands.get(player1.getId())).contains(bear);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bear);
    }

    @Test
    @DisplayName("A nonmatching creature card goes into its controller's graveyard")
    void nonmatchingCreatureGoesToGraveyard() {
        WalkingCorpse corpse = new WalkingCorpse();
        activateAndChoose(corpse, "BEAR");

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(corpse);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(corpse);
    }

    @Test
    @DisplayName("A noncreature card goes into its controller's graveyard")
    void noncreatureGoesToGraveyard() {
        Forest forest = new Forest();
        activateAndChoose(forest, "BEAR");

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("A Changeling creature card matches the chosen type")
    void changelingMatchesChosenType() {
        AvianChangeling changeling = new AvianChangeling();
        activateAndChoose(changeling, "BEAR");

        assertThat(gd.playerHands.get(player1.getId())).contains(changeling);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(changeling);
    }

    private void activateAndChoose(com.github.laxika.magicalvibes.model.Card topCard, String subtype) {
        addCreatureReady(player1, new BloodlineShaman());
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype);
    }
}
