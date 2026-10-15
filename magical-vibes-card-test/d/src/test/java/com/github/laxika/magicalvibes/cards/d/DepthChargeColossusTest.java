package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DepthChargeColossus.class})
class DepthChargeColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Prototype cast uses the alternate characteristics")
    void prototypeCastUsesAlternateCharacteristics() {
        harness.setHand(player1, List.of(new DepthChargeColossus()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();

        Permanent colossus = findPermanent(player1, "Depth Charge Colossus");
        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, colossus)).isEqualTo(6);
        assertThat(gqs.getEffectiveColors(gd, colossus)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Tapped Depth Charge Colossus does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent colossus = addColossusReady(player1);
        colossus.tap();

        harness.performUntapStep(player1);

        assertThat(colossus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{3}: Untap this creature untaps it")
    void untapAbilityUntapsIt() {
        Permanent colossus = addColossusReady(player1);
        colossus.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(colossus.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap ability cannot be activated without enough mana")
    void untapAbilityNeedsThreeMana() {
        Permanent colossus = addColossusReady(player1);
        colossus.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Prototype retains its untap restriction and activated ability, even while summoning sick")
    void prototypeRetainsUntapAbilities() {
        harness.setHand(player1, List.of(new DepthChargeColossus()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();

        Permanent colossus = findPermanent(player1, "Depth Charge Colossus");
        colossus.tap();
        harness.performUntapStep(player1);
        assertThat(colossus.isTapped()).isTrue();
        colossus.setSummoningSick(true);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        assertThat(colossus.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(colossus.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap ability untaps only its source, leaving other copies tapped")
    void untapAbilityOnlyUntapsItsSource() {
        Permanent source = addColossusReady(player1);
        Permanent other = addColossusReady(player1);
        Permanent opposing = addColossusReady(player2);
        source.tap();
        other.tap();
        opposing.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        assertThat(source.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(source.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
        assertThat(opposing.isTapped()).isTrue();
    }
    private Permanent addColossusReady(Player player) {
        return addCreatureReady(player, new DepthChargeColossus());
    }

}
