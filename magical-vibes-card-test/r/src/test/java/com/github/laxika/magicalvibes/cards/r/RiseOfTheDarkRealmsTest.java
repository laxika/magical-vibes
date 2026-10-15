package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AcademyRaider;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.i.IntoTheWilds;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.OgreBattledriver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiseOfTheDarkRealms.class, GiantSpider.class, AcademyRaider.class, IntoTheWilds.class,
        Mountain.class, OgreBattledriver.class})
class RiseOfTheDarkRealmsTest extends BaseCardTest {

    private void castRiseOfTheDarkRealms() {
        harness.castFromHand(player1, new RiseOfTheDarkRealms(), "{7}{B}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Returns creatures from the controller's graveyard to the battlefield")
    void returnsFromControllerGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card creature = new GiantSpider();
        gd.playerGraveyards.get(player1.getId()).add(creature);

        castRiseOfTheDarkRealms();

        harness.assertOnBattlefield(player1, "Giant Spider");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Returns creatures from an opponent's graveyard under the caster's control")
    void returnsOpponentCreaturesUnderCasterControl() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card opponentCreature = new GiantSpider();
        gd.playerGraveyards.get(player2.getId()).add(opponentCreature);

        castRiseOfTheDarkRealms();

        harness.assertOnBattlefield(player1, "Giant Spider");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returns creatures from both graveyards at once, all under the caster's control")
    void returnsFromBothGraveyards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        gd.playerGraveyards.get(player1.getId()).add(new GiantSpider());
        gd.playerGraveyards.get(player2.getId()).add(new AcademyRaider());

        castRiseOfTheDarkRealms();

        harness.assertOnBattlefield(player1, "Giant Spider");
        harness.assertOnBattlefield(player1, "Academy Raider");
        harness.assertNotOnBattlefield(player2, "Academy Raider");
    }

    @Test
    @DisplayName("Leaves non-creature cards in the graveyards")
    void leavesNonCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card enchantment = new IntoTheWilds();
        Card land = new Mountain();
        Card creature = new GiantSpider();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(enchantment, land, creature));

        castRiseOfTheDarkRealms();

        harness.assertOnBattlefield(player1, "Giant Spider");
        harness.assertNotOnBattlefield(player1, "Into the Wilds");
        harness.assertNotOnBattlefield(player1, "Mountain");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(enchantment, land)
                .anyMatch(c -> c.getName().equals("Rise of the Dark Realms"))
                .hasSize(3);
    }

    @Test
    @DisplayName("Returned creatures see other creatures entering simultaneously")
    void returnedCreatureTriggersForEarlierCardInGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player2, List.of(new GiantSpider(), new OgreBattledriver()));

        castRiseOfTheDarkRealms();

        harness.assertOnBattlefield(player1, "Giant Spider");
        harness.assertOnBattlefield(player1, "Ogre Battledriver");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        var spider = findPermanent(player1, "Giant Spider");
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(4);
        assertThat(spider.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Resolves harmlessly with empty graveyards")
    void worksWithEmptyGraveyards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        castRiseOfTheDarkRealms();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1)
                .anyMatch(c -> c.getName().equals("Rise of the Dark Realms"));
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
