package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThroneOfTheHighCity.class})
class ThroneOfTheHighCityTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C} produces colorless mana")
    void tapForColorless() {
        addReadyThrone(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("{4}, {T}, Sacrifice this land makes its controller the monarch")
    void sacrificesAndMakesControllerMonarch() {
        Permanent throne = addReadyThrone(player1);
        gd.monarchPlayerId = player2.getId();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(throne.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Throne of the High City"));
    }

    @Test
    void cannotActivateMonarchAbilityWithoutFourMana() {
        Permanent throne = addReadyThrone(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(throne.isTapped()).isFalse();
        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    void sacrificeAndManaArePaidBeforeMonarchAbilityResolves() {
        Permanent throne = addReadyThrone(player1);
        gd.monarchPlayerId = player2.getId();
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(throne.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Throne of the High City");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canBecomeFirstMonarchOnOpponentsTurn() {
        addReadyThrone(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        harness.assertInGraveyard(player1, "Throne of the High City");
    }

    @Test
    void tappedThroneCannotActivateEitherAbility() {
        Permanent throne = addReadyThrone(player1);
        throne.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Throne of the High City");
        harness.assertNotInGraveyard(player1, "Throne of the High City");
        assertThat(gd.monarchPlayerId).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void newlyEnteredNoncreatureLandCanActivateMonarchAbility() {
        Permanent throne = addReadyThrone(player1);
        throne.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        harness.assertInGraveyard(player1, "Throne of the High City");
    }

    private Permanent addReadyThrone(Player player) {
        Permanent permanent = addCreatureReady(player, new ThroneOfTheHighCity());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }
}
