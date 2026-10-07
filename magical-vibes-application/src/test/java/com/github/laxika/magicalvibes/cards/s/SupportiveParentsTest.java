package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SupportiveParents.class})
class SupportiveParentsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping two untapped creatures adds one mana of the chosen color")
    void tapsTwoCreaturesForAnyColorMana() {
        Permanent source = addCreatureReady(player1, new SupportiveParents());
        Permanent creature = addCreatureReady(player1, new SupportiveParents());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, 0, null, null);

        assertThat(source.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without two untapped creatures you control")
    void cannotActivateWithoutTwoCreatures() {
        Permanent source = addCreatureReady(player1, new SupportiveParents());
        addCreatureReady(player2, new SupportiveParents());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Summoning-sick creatures can pay the tap cost")
    void canTapSummoningSickCreatures() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SupportiveParents());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SupportiveParents());
        source.setSummoningSick(true);
        creature.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(source.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped source can activate using two other creatures")
    void canActivateWhileSourceIsTapped() {
        Permanent source = addCreatureReady(player1, new SupportiveParents());
        source.tap();
        Permanent first = addCreatureReady(player1, new SupportiveParents());
        Permanent second = addCreatureReady(player1, new SupportiveParents());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(source.isTapped()).isTrue();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Already tapped creatures cannot pay the cost")
    void cannotCountTappedCreatures() {
        Permanent source = addCreatureReady(player1, new SupportiveParents());
        Permanent creature = addCreatureReady(player1, new SupportiveParents());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller may choose two other creatures and leave the source untapped")
    void canChooseOtherCreaturesToPayCost() {
        Permanent source = addCreatureReady(player1, new SupportiveParents());
        Permanent first = addCreatureReady(player1, new SupportiveParents());
        Permanent second = addCreatureReady(player1, new SupportiveParents());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handleListChoice(player1, "RED");

        assertThat(source.isTapped()).isFalse();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
