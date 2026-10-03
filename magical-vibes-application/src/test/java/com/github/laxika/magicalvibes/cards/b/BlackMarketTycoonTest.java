package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlackMarketTycoon.class})
class BlackMarketTycoonTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure token when its ability resolves")
    void createsTreasureToken() {
        Permanent tycoon = addCreatureReady(player1, new BlackMarketTycoon());

        harness.activateAbility(player1, indexOf(tycoon), null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Deals two damage for each Treasure controlled at upkeep")
    void dealsDamageForEachTreasureControlled() {
        addCreatureReady(player1, new BlackMarketTycoon());
        addTreasure(player1);
        addTreasure(player1);
        addTreasure(player2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Counts Treasures when the upkeep ability resolves")
    void countsTreasuresAtResolution() {
        addCreatureReady(player1, new BlackMarketTycoon());

        advanceToUpkeep(player1);
        addTreasure(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        addCreatureReady(player1, new BlackMarketTycoon());
        addTreasure(player1);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void dealsNoDamageWithoutTreasures() {
        addCreatureReady(player1, new BlackMarketTycoon());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent tycoon = addCreatureReady(player1, new BlackMarketTycoon());
        tycoon.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(tycoon), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void tappingIsPaidBeforeTokenCreationResolves() {
        Permanent tycoon = addCreatureReady(player1, new BlackMarketTycoon());

        harness.activateAbility(player1, indexOf(tycoon), null, null);

        assertThat(tycoon.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(tycoon), null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
    }

    @Test
    void canCreateTreasureInResponseToZeroTreasureUpkeepTrigger() {
        Permanent tycoon = addCreatureReady(player1, new BlackMarketTycoon());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.activateAbility(player1, indexOf(tycoon), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void sacrificingTreasureInResponseReducesUpkeepDamage() {
        Permanent tycoon = addCreatureReady(player1, new BlackMarketTycoon());
        harness.activateAbility(player1, indexOf(tycoon), null, null);
        harness.passBothPriorities();
        Permanent treasure = findPermanent(player1, "Treasure");

        advanceToUpkeep(player1);
        harness.activateAbility(player1, indexOf(treasure), null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.assertLife(player1, 20);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private Permanent addTreasure(Player player) {
        Card card = new Card();
        card.setName("Treasure");
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.TREASURE));
        card.setToken(true);

        return addCreatureReady(player, card);
    }
}
