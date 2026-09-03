package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PoliticalTrickery.class, Forest.class, Island.class, ZhalfirinKnight.class})
class PoliticalTrickeryTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new PoliticalTrickery()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Exchanges control of the two target lands")
    void exchangesControlOfLands() {
        prepare();
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
        prepare();
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
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent alsoOwn = harness.addToBattlefieldAndReturn(player1, new Island());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(own.getId(), alsoOwn.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land an opponent controls");

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target an opponent's land as the land the caster controls")
    void cannotTargetOpponentsLandAsOwnTarget() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opponents.getId(), own.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("First target must be a land you control");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent as the opponent's land")
    void cannotTargetNonlandAsOpponentTarget() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent nonLand = harness.addToBattlefieldAndReturn(player2, new ZhalfirinKnight());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(own.getId(), nonLand.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Second target must be a land an opponent controls");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent as the land the caster controls")
    void cannotTargetNonlandAsOwnTarget() {
        prepare();
        Permanent nonLand = harness.addToBattlefieldAndReturn(player1, new ZhalfirinKnight());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(nonLand.getId(), opponents.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("First target must be a land you control");
    }
}
