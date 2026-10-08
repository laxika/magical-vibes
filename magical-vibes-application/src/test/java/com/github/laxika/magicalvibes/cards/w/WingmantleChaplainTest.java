package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BalduvianBerserker;
import com.github.laxika.magicalvibes.cards.c.CoralColony;
import com.github.laxika.magicalvibes.cards.o.OneWithTheStars;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WingmantleChaplain.class, CoralColony.class, BalduvianBerserker.class, OneWithTheStars.class})
class WingmantleChaplainTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one Bird for each creature with defender you control")
    void entersAndCreatesBirdsForControlledDefenders() {
        harness.addToBattlefield(player1, new CoralColony());
        castChaplain();

        assertThat(countPermanents(player1, "Bird")).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates a Bird when another creature with defender enters under your control")
    void createsBirdWhenAnotherDefenderEnters() {
        castChaplain();
        harness.setHand(player1, List.of(new CoralColony()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird")).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a creature without defender")
    void doesNotTriggerForNonDefender() {
        castChaplain();
        harness.enterBattlefieldAndReturn(player1, new BalduvianBerserker());

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Bird")).isEqualTo(1);
    }

    @Test
    void doesNotCountOpponentsDefenders() {
        harness.addToBattlefield(player2, new CoralColony());
        castChaplain();

        assertThat(countPermanents(player1, "Bird")).isEqualTo(1);
        assertThat(countPermanents(player2, "Bird")).isZero();
    }

    @Test
    void doesNotTriggerForOpponentsDefenderEntering() {
        castChaplain();
        harness.enterBattlefieldAndReturn(player2, new CoralColony());

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Bird")).isEqualTo(1);
    }

    @Test
    void countsDefendersAtResolutionAndCreatesNothingWhenNoneRemain() {
        Permanent chaplain = harness.enterBattlefieldAndReturn(player1, new WingmantleChaplain());
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(chaplain);
        gd.playerGraveyards.get(player1.getId()).add(chaplain.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird")).isZero();
    }

    @Test
    void defenderEntryTriggerStillResolvesAfterEnteringDefenderLeaves() {
        castChaplain();
        Permanent colony = harness.enterBattlefieldAndReturn(player1, new CoralColony());
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(colony);
        gd.playerGraveyards.get(player1.getId()).add(colony.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bird")).isEqualTo(2);
    }

    @Test
    void doesNotCountNoncreaturePermanentsThatRetainDefender() {
        Permanent colony = harness.addToBattlefieldAndReturn(player1, new CoralColony());
        harness.setHand(player1, List.of(new OneWithTheStars()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, colony.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, colony)).isFalse();
        assertThat(gqs.hasKeyword(gd, colony, Keyword.DEFENDER)).isTrue();

        castChaplain();

        assertThat(countPermanents(player1, "Bird")).isEqualTo(1);
    }

    @Test
    void createsWhiteFlyingOneOneBirdCreatureTokens() {
        castChaplain();

        assertThat(findPermanents(player1, "Bird")).singleElement().satisfies(bird -> {
            assertThat(bird.getCard().isToken()).isTrue();
            assertThat(gqs.isCreature(gd, bird)).isTrue();
            assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, bird, Keyword.FLYING)).isTrue();
            assertThat(bird.getCard().getColor()).isEqualTo(CardColor.WHITE);
        });
    }

    @Test
    void anotherChaplainTriggersExistingChaplainAndCountsBothDefenders() {
        castChaplain();
        castChaplain();

        assertThat(countPermanents(player1, "Bird")).isEqualTo(4);
    }

    private void castChaplain() {
        harness.setHand(player1, List.of(new WingmantleChaplain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
