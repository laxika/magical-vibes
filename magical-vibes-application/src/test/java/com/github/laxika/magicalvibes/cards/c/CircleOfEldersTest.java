package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DragonlordAtarka;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CircleOfElders.class, DragonlordAtarka.class})
class CircleOfEldersTest extends BaseCardTest {

    @Test
    @DisplayName("Adds three colorless mana when your creatures have total power 8 or greater")
    void addsThreeColorlessManaWhenFormidable() {
        Permanent circle = addCreatureReady(player1, new CircleOfElders());
        addCreatureReady(player1, new DragonlordAtarka());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(circle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate at exactly eight total power")
    void canActivateAtExactThreshold() {
        Permanent circle = addCreatureReady(player1, new CircleOfElders());
        addCreatureReady(player1, new DragonlordAtarka());
        circle.setPowerModifier(-2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate below eight total power")
    void cannotActivateBelowThreshold() {
        Permanent circle = addCreatureReady(player1, new CircleOfElders());
        addCreatureReady(player1, new DragonlordAtarka());
        circle.setPowerModifier(-3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power 8 or greater");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(circle.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Counts only creatures you control")
    void countsOnlyControlledCreatures() {
        Permanent circle = addCreatureReady(player1, new CircleOfElders());
        addCreatureReady(player2, new DragonlordAtarka());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power 8 or greater");
        assertThat(circle.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapped creatures still contribute to formidable and the mana ability uses no stack")
    void tappedCreaturesContributePower() {
        Permanent circle = addCreatureReady(player1, new CircleOfElders());
        Permanent support = addCreatureReady(player1, new DragonlordAtarka());
        support.tap();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThat(circle.isTapped()).isTrue();
        assertThat(support.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick even when formidable is met")
    void cannotActivateWhileSummoningSick() {
        Permanent circle = harness.addToBattlefieldAndReturn(player1, new CircleOfElders());
        circle.setSummoningSick(true);
        addCreatureReady(player1, new DragonlordAtarka());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(circle.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate again without untapping")
    void cannotActivateTwiceWhileTapped() {
        addCreatureReady(player1, new CircleOfElders());
        addCreatureReady(player1, new DragonlordAtarka());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Vigilance leaves Circle untapped after attacking")
    void remainsUntappedAfterAttacking() {
        Permanent circle = addCreatureReady(player1, new CircleOfElders());
        addCreatureReady(player1, new DragonlordAtarka());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(circle.isTapped()).isFalse();
    }
}
