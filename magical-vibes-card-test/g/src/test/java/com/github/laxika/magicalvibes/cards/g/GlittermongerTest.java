package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Glittermonger.class})
class GlittermongerTest extends BaseCardTest {

    @Test
    void tapsToCreateTreasureToken() {
        Permanent glittermonger = harness.addToBattlefieldAndReturn(player1, new Glittermonger());
        glittermonger.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(glittermonger.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new Glittermonger());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        Permanent glittermonger = harness.addToBattlefieldAndReturn(player1, new Glittermonger());
        glittermonger.setSummoningSick(false);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void tokenCreationUsesTheStackAndSurvivesSourceLeaving() {
        Permanent glittermonger = harness.addToBattlefieldAndReturn(player1, new Glittermonger());
        glittermonger.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(glittermonger.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        gd.playerBattlefields.get(player1.getId()).remove(glittermonger);
        gd.playerGraveyards.get(player1.getId()).add(glittermonger.getCard());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void treasureCanImmediatelyBeSacrificedForAnyColor(ManaColor color) {
        Permanent glittermonger = harness.addToBattlefieldAndReturn(player1, new Glittermonger());
        glittermonger.setSummoningSick(false);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent treasure = findPermanents(player1, "Treasure").getFirst();
        assertThat(treasure.isTapped()).isFalse();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(treasure), null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
