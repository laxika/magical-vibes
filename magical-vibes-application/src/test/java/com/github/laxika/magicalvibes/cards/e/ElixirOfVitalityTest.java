package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElixirOfVitality.class})
class ElixirOfVitalityTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new ElixirOfVitality()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent elixir = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(elixir.isTapped()).isTrue();
    }

    @Test
    @DisplayName("First ability: tap and sacrifice to gain 4 life")
    void cheapAbilityGainsFourLife() {
        harness.addToBattlefield(player1, new ElixirOfVitality());

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Second ability: {8}, tap and sacrifice to gain 8 life")
    void expensiveAbilityGainsEightLife() {
        harness.addToBattlefield(player1, new ElixirOfVitality());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 8);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Second ability cannot be activated without {8}")
    void expensiveAbilityRequiresMana() {
        harness.addToBattlefield(player1, new ElixirOfVitality());

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Abilities cannot be activated while Elixir of Vitality is tapped")
    void abilitiesRequireUntappedArtifact() {
        Permanent elixir = harness.addToBattlefieldAndReturn(player1, new ElixirOfVitality());
        elixir.tap();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(elixir);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Second ability requires all eight generic mana")
    void expensiveAbilityRequiresAllEightMana() {
        harness.addToBattlefield(player1, new ElixirOfVitality());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(7);
    }
}
