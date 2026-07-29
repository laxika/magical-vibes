package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PoliticalTrickeryTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges control of the two target lands")
    void exchangesControlOfLands() {
        harness.setHand(player1, List.of(new PoliticalTrickery()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opponents.getId()));

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("Exchange fizzles when a target land leaves the battlefield before resolution")
    void fizzlesWhenTargetGone() {
        harness.setHand(player1, List.of(new PoliticalTrickery()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.castSorcery(player1, 0, List.of(own.getId(), opponents.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(opponents);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a land the caster controls as the opponent's land")
    void cannotTargetOwnLandAsOpponentTarget() {
        harness.setHand(player1, List.of(new PoliticalTrickery()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent alsoOwn = harness.addToBattlefieldAndReturn(player1, new Island());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(own.getId(), alsoOwn.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land an opponent controls");

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Forest");
    }
}
