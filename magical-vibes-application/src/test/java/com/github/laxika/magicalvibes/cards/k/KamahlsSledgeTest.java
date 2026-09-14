package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AngelOfRetribution;
import com.github.laxika.magicalvibes.cards.a.AvenTrooper;
import com.github.laxika.magicalvibes.cards.t.TaintedIsle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KamahlsSledge.class, AngelOfRetribution.class, AvenTrooper.class, TaintedIsle.class})
class KamahlsSledgeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals exactly 4 damage to a target creature without threshold")
    void dealsDamageWithoutThreshold() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution());
        castAndResolveAtTarget(target);

        harness.assertOnBattlefield(player2, "Angel of Retribution");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("With threshold, also deals 4 damage to the target creature's controller")
    void thresholdAlsoDamagesController() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution());
        castAndResolveAtTarget(target);

        harness.assertOnBattlefield(player2, "Angel of Retribution");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("With threshold, damages the controller even when the target creature dies")
    void thresholdDamagesControllerWhenTargetDies() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenTrooper());
        castAndResolveAtTarget(target);

        harness.assertNotOnBattlefield(player2, "Aven Trooper");
        harness.assertInGraveyard(player2, "Aven Trooper");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Only the caster's graveyard enables threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution());
        castAndResolveAtTarget(target);

        harness.assertOnBattlefield(player2, "Angel of Retribution");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Threshold is checked when the spell resolves")
    void thresholdCanBeLostBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelOfRetribution());
        harness.setGraveyard(player1, graveyardWithSevenCards());
        prepareSledge();
        harness.castSorcery(player1, 0, target.getId());

        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Angel of Retribution");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TaintedIsle());
        prepareSledge();

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castAndResolveAtTarget(Permanent target) {
        prepareSledge();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void prepareSledge() {
        harness.setHand(player1, List.of(new KamahlsSledge()));
        addMana();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new AngelOfRetribution(), new AngelOfRetribution(), new AngelOfRetribution(),
                new AngelOfRetribution(), new AngelOfRetribution(), new AngelOfRetribution(),
                new AngelOfRetribution());
    }
}
