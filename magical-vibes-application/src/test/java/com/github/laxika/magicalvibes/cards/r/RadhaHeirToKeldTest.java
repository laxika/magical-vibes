package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RadhaHeirToKeld.class, GrizzlyBears.class})
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
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.DECLARE_ATTACKERS));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.DECLARE_ATTACKERS));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
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
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
