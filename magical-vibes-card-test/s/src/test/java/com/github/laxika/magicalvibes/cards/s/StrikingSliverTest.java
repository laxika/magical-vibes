package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrikingSliver.class, BonescytheSliver.class, ElvishMystic.class, Shock.class})
class StrikingSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Striking Sliver grants itself first strike (it is a Sliver)")
    void grantsSelfFirstStrike() {
        Permanent sliver = addCreatureReady(player1, new StrikingSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Grants first strike to another Sliver you control")
    void grantsFirstStrikeToOtherSliver() {
        addCreatureReady(player1, new StrikingSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant first strike to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new StrikingSliver());
        Permanent nonSliver = addCreatureReady(player1, new ElvishMystic());

        assertThat(gqs.hasKeyword(gd, nonSliver, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant first strike to an opponent's Sliver")
    void doesNotGrantToOpponentSliver() {
        addCreatureReady(player1, new StrikingSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike kills a blocker before it can deal combat damage")
    void killsBlockerBeforeItDealsDamage() {
        Permanent sliver = addCreatureReady(player1, new StrikingSliver());
        addCreatureReady(player2, new ElvishMystic());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Striking Sliver");
        harness.assertInGraveyard(player2, "Elvish Mystic");
        assertThat(sliver.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Other Slivers lose first strike when Striking Sliver leaves the battlefield")
    void losesFirstStrikeWhenSourceLeaves() {
        Permanent source = addCreatureReady(player1, new StrikingSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FIRST_STRIKE)).isTrue();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, source.getId());

        harness.assertInGraveyard(player1, "Striking Sliver");
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.DOUBLE_STRIKE)).isTrue();
    }
}
