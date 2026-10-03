package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.d.Disperse;
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

@CardUsed({BlurSliver.class, BonescytheSliver.class, CoralMerfolk.class, Disperse.class})
class BlurSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Blur Sliver grants itself haste (it is a Sliver)")
    void grantsSelfHaste() {
        Permanent sliver = addCreatureReady(player1, new BlurSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Grants haste to another Sliver you control")
    void grantsHasteToOtherSliver() {
        addCreatureReady(player1, new BlurSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant haste to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new BlurSliver());
        Permanent merfolk = addCreatureReady(player1, new CoralMerfolk());

        assertThat(gqs.hasKeyword(gd, merfolk, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant haste to an opponent's Sliver")
    void doesNotGrantToOpponentSliver() {
        addCreatureReady(player1, new BlurSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Newly controlled Slivers can attack immediately")
    void newlyControlledSliversCanAttack() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BlurSliver());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BonescytheSliver());
        source.setSummoningSick(true);
        other.setSummoningSick(true);

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThat(source.isAttacking()).isTrue();
        assertThat(other.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Haste begins only when Blur Sliver resolves")
    void hasteBeginsOnResolution() {
        Permanent other = addCreatureReady(player1, new BonescytheSliver());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new BlurSliver(), "{2}{R}");

        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Blur Sliver"), Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste remains until the last Blur Sliver leaves the battlefield")
    void hasteEndsWhenLastSourceLeaves() {
        Permanent first = addCreatureReady(player1, new BlurSliver());
        Permanent second = addCreatureReady(player1, new BlurSliver());
        Permanent other = addCreatureReady(player1, new BonescytheSliver());
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isTrue();
        harness.setHand(player1, List.of(new Disperse(), new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, first.getId());

        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isTrue();

        harness.castAndResolveInstant(player1, 0, second.getId());

        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isFalse();
    }
}
