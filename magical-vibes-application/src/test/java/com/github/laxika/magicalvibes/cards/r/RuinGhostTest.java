package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuinGhostTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers a land you control and taps Ruin Ghost")
    void flickersOwnLand() {
        harness.addToBattlefield(player1, new RuinGhost());
        findPermanent(player1, "Ruin Ghost").setSummoningSick(false);
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(harness.getPermanentId(player1, "Forest")).isNotEqualTo(forestId);
        assertThat(findPermanent(player1, "Ruin Ghost").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a land controlled by an opponent")
    void cannotTargetOpponentLand() {
        harness.addToBattlefield(player1, new RuinGhost());
        findPermanent(player1, "Ruin Ghost").setSummoningSick(false);
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID opponentForestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentForestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new RuinGhost());
        findPermanent(player1, "Ruin Ghost").setSummoningSick(false);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }
}
