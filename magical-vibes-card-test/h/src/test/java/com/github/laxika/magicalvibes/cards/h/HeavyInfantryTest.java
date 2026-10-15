package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeavyInfantry.class, TimberpackWolf.class})
class HeavyInfantryTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps target creature an opponent controls")
    void tapsTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());

        castHeavyInfantry(player2, "Timberpack Wolf");
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isZero();
        harness.assertOnBattlefield(player1, "Heavy Infantry");
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        UUID ownBearId = harness.addToBattlefieldAndReturn(player1, new TimberpackWolf()).getId();
        UUID opponentWolfId = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf()).getId();
        harness.castFromHand(player1, new HeavyInfantry(), "{4}{W}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBearId))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentWolfId);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can enter when there are no opponent creatures to target")
    void entersWithoutLegalTarget() {
        harness.addToBattlefield(player1, new TimberpackWolf());
        harness.castFromHand(player1, new HeavyInfantry(), "{4}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Heavy Infantry");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already tapped opponent creature remains a legal target")
    void canTargetTappedCreature() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());
        wolf.tap();

        castHeavyInfantry(player2, "Timberpack Wolf");
        harness.passBothPriorities();

        assertThat(wolf.isTapped()).isTrue();
        assertThat(wolf.getSkipUntapCount()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Heavy Infantry");
    }

    @Test
    @DisplayName("Chooses a creature that appeared after Heavy Infantry was cast")
    void choosesTargetAfterEntering() {
        harness.castFromHand(player1, new HeavyInfantry(), "{4}{W}");
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, wolf.getId());
        harness.passBothPriorities();

        assertThat(wolf.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Heavy Infantry");
        assertThat(gd.stack).isEmpty();
    }

    private void castHeavyInfantry(Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.castFromHand(player1, new HeavyInfantry(), "{4}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
    }
}
