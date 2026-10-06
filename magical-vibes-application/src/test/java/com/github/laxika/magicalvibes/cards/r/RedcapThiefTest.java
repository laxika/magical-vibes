package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RedcapThief.class})
class RedcapThiefTest extends BaseCardTest {

    @Test
    @DisplayName("When Redcap Thief enters, it creates a Treasure token")
    void etbCreatesTreasureToken() {
        harness.castFromHand(player1, new RedcapThief(), "{2}{R}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void treasureIsCreatedOnlyWhenTheEnterTriggerResolves() {
        harness.castFromHand(player1, new RedcapThief(), "{2}{R}");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Redcap Thief");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void enteringWithoutBeingCastCreatesTreasureForItsController() {
        harness.enterBattlefieldAndReturn(player2, new RedcapThief());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void treasureCanImmediatelyBeSacrificedForOneManaOfAnyColor(ManaColor color) {
        harness.castFromHand(player1, new RedcapThief(), "{2}{R}");
        resolveAllTriggers();
        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isFalse();
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(color);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(treasure), 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(manaBefore + 1);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Redcap Thief");
    }
}
