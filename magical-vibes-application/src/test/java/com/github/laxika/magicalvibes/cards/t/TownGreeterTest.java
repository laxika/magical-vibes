package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TownGreeter.class, Forest.class, Shock.class})
class TownGreeterTest extends BaseCardTest {

    @Test
    void putsMilledTownIntoHandAndGainsLife() {
        Card town = townLand();
        harness.setLibrary(player1, List.of(town, new Shock(), new Shock(), new Shock()));

        castTownGreeter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(town);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void putsMilledNonTownLandIntoHandWithoutGainingLife() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new Shock(), new Shock(), new Shock()));

        castTownGreeter();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void decliningTheTownDoesNotGainLifeOrReturnIt() {
        Card town = townLand();
        harness.setLibrary(player1, List.of(town, new Shock(), new Shock(), new Shock()));

        castTownGreeter();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(town);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(town);
    }

    private void castTownGreeter() {
        harness.setHand(player1, List.of(new TownGreeter()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Card townLand() {
        Permanent townPermanent = new Permanent(new Forest());
        Card town = TestCards.mutableCard(townPermanent);
        town.setSubtypes(List.of(CardSubtype.TOWN));
        return town;
    }
}
