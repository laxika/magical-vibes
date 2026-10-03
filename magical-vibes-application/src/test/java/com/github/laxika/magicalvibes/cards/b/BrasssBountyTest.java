package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrasssBounty.class, Forest.class, Island.class})
class BrasssBountyTest extends BaseCardTest {

    @Test
    void createsOneTreasureForEachLandYouControl() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new BrasssBounty()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void createsNoTreasuresWithoutLandsEvenWhenOpponentControlsLands() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new BrasssBounty()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        harness.assertInGraveyard(player1, "Brass's Bounty");
    }

    @Test
    void countsLandsAtResolutionIncludingTappedLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new BrasssBounty()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castSorcery(player1, 0, 0);

        harness.addToBattlefield(player1, new Island());
        findPermanent(player1, "Island").setTapped(true);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void createdTreasureCanImmediatelyBeSacrificedForAnyColor(ManaColor color) {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new BrasssBounty()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castAndResolveSorcery(player1, 0, 0);

        var treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isFalse();
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
