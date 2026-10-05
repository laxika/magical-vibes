package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TempleGarden;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PeopleOfTheWoods.class, Forest.class, GloriousAnthem.class, Plains.class, TempleGarden.class})
class PeopleOfTheWoodsTest extends BaseCardTest {

    @Test
    @DisplayName("People of the Woods has power 1 and toughness 0 with no Forests")
    void hasOnePowerAndZeroToughnessWithNoForests() {
        Permanent people = addCreatureReady(player1, new PeopleOfTheWoods());

        assertStats(people, 1, 0);
    }

    @Test
    @DisplayName("People of the Woods toughness equals the number of Forests you control")
    void toughnessEqualsControlledForests() {
        Permanent people = addCreatureReady(player1, new PeopleOfTheWoods());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Forest());

        assertStats(people, 1, 2);
    }

    @Test
    @DisplayName("People of the Woods updates when your Forests change")
    void toughnessUpdatesWhenForestsChange() {
        Permanent people = addCreatureReady(player1, new PeopleOfTheWoods());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertStats(people, 1, 1);

        harness.addToBattlefield(player1, new Forest());
        assertStats(people, 1, 2);

        gd.playerBattlefields.get(player1.getId()).remove(forest);
        assertStats(people, 1, 1);
    }

    @Test
    @DisplayName("People of the Woods keeps its power while static bonuses apply")
    void powerRemainsOneWithStaticBonus() {
        Permanent people = addCreatureReady(player1, new PeopleOfTheWoods());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertStats(people, 2, 3);
    }

    @Test
    @CardUsed({TempleGarden.class})
    @DisplayName("People of the Woods counts nonbasic lands with the Forest subtype")
    void countsNonbasicForestSubtype() {
        Permanent people = addCreatureReady(player1, new PeopleOfTheWoods());
        harness.addToBattlefield(player1, new TempleGarden());

        assertStats(people, 1, 1);
    }

    @Test
    @DisplayName("People of the Woods counts its current controller's Forests")
    void toughnessChangesWithController() {
        Permanent people = addCreatureReady(player1, new PeopleOfTheWoods());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertStats(people, 1, 1);

        gd.playerBattlefields.get(player1.getId()).remove(people);
        gd.playerBattlefields.get(player2.getId()).add(people);

        assertStats(people, 1, 2);
    }

    @Test
    @DisplayName("People of the Woods defines its toughness in the graveyard")
    void toughnessIsDefinedInGraveyard() {
        PeopleOfTheWoods people = new PeopleOfTheWoods();
        harness.setGraveyard(player1, List.of(people));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectiveCardToughness(gd, people)).isEqualTo(1);

        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectiveCardToughness(gd, people)).isEqualTo(2);
    }

    @Test
    @DisplayName("People of the Woods dies on entering without any Forests")
    void diesWhenCastWithoutForests() {
        harness.castFromHand(player1, new PeopleOfTheWoods(), "{G}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "People of the Woods");
        harness.assertInGraveyard(player1, "People of the Woods");
    }

    private void assertStats(Permanent people, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, people)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, people)).isEqualTo(toughness);
    }
}
