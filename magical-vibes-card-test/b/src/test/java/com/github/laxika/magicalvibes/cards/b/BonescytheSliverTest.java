package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.p.PredatorySliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BonescytheSliver.class, CoralMerfolk.class, Disperse.class, PredatorySliver.class})
class BonescytheSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Bonescythe Sliver grants itself double strike (it is a Sliver)")
    void grantsSelfDoubleStrike() {
        Permanent sliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Grants double strike to another Sliver you control, and revokes it when it leaves")
    void grantsDoubleStrikeToOtherSliver() {
        Permanent sliver = addCreatureReady(player1, new BonescytheSliver());
        Permanent otherSliver = addCreatureReady(player1, new PredatorySliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, sliver.getId());

        harness.assertInHand(player1, "Bonescythe Sliver");
        harness.assertOnBattlefield(player1, "Predatory Sliver");
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant double strike to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new BonescytheSliver());
        Permanent merfolk = addCreatureReady(player1, new CoralMerfolk());

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant double strike to an opponent's non-Sliver creature")
    void doesNotGrantToOpponentCreature() {
        addCreatureReady(player1, new BonescytheSliver());
        Permanent opponentMerfolk = addCreatureReady(player2, new CoralMerfolk());

        assertThat(gqs.hasKeyword(gd, opponentMerfolk, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant double strike to an opponent's Sliver")
    void doesNotGrantToOpponentSliver() {
        addCreatureReady(player1, new BonescytheSliver());
        Permanent opponentSliver = addCreatureReady(player2, new PredatorySliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Double strike begins when Bonescythe Sliver resolves")
    void doubleStrikeBeginsOnResolution() {
        Permanent other = addCreatureReady(player1, new PredatorySliver());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new BonescytheSliver(), "{3}{W}");

        assertThat(gqs.hasKeyword(gd, other, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, other, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Bonescythe Sliver"), Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Double strike remains until the last Bonescythe Sliver leaves")
    void doubleStrikeEndsWhenLastSourceLeaves() {
        Permanent first = addCreatureReady(player1, new BonescytheSliver());
        Permanent second = addCreatureReady(player1, new BonescytheSliver());
        Permanent other = addCreatureReady(player1, new PredatorySliver());
        harness.setHand(player1, List.of(new Disperse(), new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, first.getId());

        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.castAndResolveInstant(player1, 0, second.getId());

        assertThat(gqs.hasKeyword(gd, other, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Unblocked Bonescythe Sliver deals both first-strike and regular combat damage")
    void dealsDamageTwice() {
        addCreatureReady(player1, new BonescytheSliver());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Another Sliver deals both first-strike and regular combat damage")
    void otherSliverDealsDamageTwice() {
        addCreatureReady(player1, new BonescytheSliver());
        addCreatureReady(player1, new PredatorySliver());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 16);
    }
}
