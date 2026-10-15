package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuinGhost.class, Forest.class, GrizzlyBears.class})
class RuinGhostTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers a land you control and taps Ruin Ghost")
    void flickersOwnLand() {
        addCreatureReady(player1, new RuinGhost());
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
        addCreatureReady(player1, new RuinGhost());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID opponentForestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentForestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        addCreatureReady(player1, new RuinGhost());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped land returns untapped")
    void tappedLandReturnsUntapped() {
        addCreatureReady(player1, new RuinGhost());
        harness.addToBattlefield(player1, new Forest());
        findPermanent(player1, "Forest").tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Forest"));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }

    @Test
    @DisplayName("A land owned by the opponent returns under your control")
    void returnsOpponentOwnedLandUnderYourControl() {
        addCreatureReady(player1, new RuinGhost());
        Forest forest = new Forest();
        forest.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, forest);
        UUID oldId = harness.getPermanentId(player1, "Forest");
        gd.stolenCreatures.put(oldId, player2.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, oldId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(harness.getPermanentId(player1, "Forest")).isNotEqualTo(oldId);
        assertThat(findPermanent(player1, "Forest").getCard().getOwnerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("A land that changes controller before resolution is not flickered")
    void landChangingControllerBecomesIllegalTarget() {
        addCreatureReady(player1, new RuinGhost());
        harness.addToBattlefield(player1, new Forest());
        var forest = findPermanent(player1, "Forest");
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, forest.getId());
        gd.playerBattlefields.get(player1.getId()).remove(forest);
        gd.playerBattlefields.get(player2.getId()).add(forest);
        gd.stolenCreatures.put(forest.getId(), player1.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(harness.getPermanentId(player2, "Forest")).isEqualTo(forest.getId());
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new RuinGhost());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 1);
        UUID forestId = harness.getPermanentId(player1, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability requires white mana")
    void cannotActivateWithoutWhiteMana() {
        addCreatureReady(player1, new RuinGhost());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        UUID forestId = harness.getPermanentId(player1, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }
}
