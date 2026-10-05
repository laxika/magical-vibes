package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SerraSphinx;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RadhaHeirToKeld.class, SerraSphinx.class})
class RadhaHeirToKeldTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Radha produces one green mana")
    void tappingProducesGreenMana() {
        Permanent radha = addCreatureReady(player1, new RadhaHeirToKeld());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(radha.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking with Radha may add two red mana")
    void attackingMayAddTwoRedMana() {
        addCreatureReady(player1, new RadhaHeirToKeld());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Declining Radha's attack trigger adds no red mana")
    void decliningAttackTriggerAddsNoMana() {
        addCreatureReady(player1, new RadhaHeirToKeld());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Attacking with another creature does not trigger Radha")
    void anotherCreatureAttackingDoesNotTriggerRadha() {
        Permanent radha = harness.addToBattlefieldAndReturn(player1, new RadhaHeirToKeld());
        radha.setSummoningSick(true);
        addCreatureReady(player1, new SerraSphinx());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void summoningSickRadhaCannotTapForMana() {
        Permanent radha = harness.addToBattlefieldAndReturn(player1, new RadhaHeirToKeld());
        radha.setSummoningSick(true);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(radha.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void tappedRadhaCannotProduceManaAgain() {
        addCreatureReady(player1, new RadhaHeirToKeld());
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void attackTriggerUsesStackAndAwardsOnlyAttackingControllersMana() {
        Permanent radha = addCreatureReady(player2, new RadhaHeirToKeld());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));

            assertThat(gd.stack).hasSize(1);
            assertThat(radha.isTapped()).isTrue();
            assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
            assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();

            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player2, true);

            assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(2);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        });
    }
}
