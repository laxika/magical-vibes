package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AshnodsAltar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EvinWaterdeepOpportunist.class, AshnodsAltar.class, GrizzlyBears.class})
class EvinWaterdeepOpportunistTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 for each Treasure you control")
    void scalesPowerWithTreasures() {
        Permanent evin = harness.addToBattlefieldAndReturn(player1, new EvinWaterdeepOpportunist());
        addTreasureToken(player1);
        addTreasureToken(player1);

        assertThat(gqs.getEffectivePower(gd, evin)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, evin)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creates one tapped Treasure when a creature is sacrificed, once each turn")
    void createsTappedTreasureOnceEachTurn() {
        harness.addToBattlefield(player1, new EvinWaterdeepOpportunist());
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new AshnodsAltar());
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(altar), null, null);
        harness.handlePermanentChosen(player1, firstBear.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isTrue();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(altar), null, null);
        harness.handlePermanentChosen(player1, secondBear.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    private void addTreasureToken(com.github.laxika.magicalvibes.model.Player player) {
        Card treasureCard = new Card();
        treasureCard.setName("Treasure");
        treasureCard.setType(CardType.ARTIFACT);
        treasureCard.setToken(true);
        treasureCard.setSubtypes(List.of(CardSubtype.TREASURE));
        harness.addToBattlefield(player, treasureCard);
    }
}
