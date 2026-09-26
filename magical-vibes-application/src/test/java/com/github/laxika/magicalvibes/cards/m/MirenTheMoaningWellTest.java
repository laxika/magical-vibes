package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EiganjoFreeRiders;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirenTheMoaningWell.class, EiganjoFreeRiders.class})
class MirenTheMoaningWellTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds colorless mana")
    void tappingForManaAddsColorless() {
        Permanent miren = addReadyMiren(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(miren.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing a creature gains life equal to its toughness")
    void sacrificingCreatureGainsLifeEqualToToughness() {
        Permanent miren = addReadyMiren(player1);
        Permanent creature = addCreatureReady(player1, new EiganjoFreeRiders());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        if (gd.interaction.activeInteraction() != null) {
            harness.handlePermanentChosen(player1, creature.getId());
        }
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(miren.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Eiganjo Free-Riders");
    }

    @Test
    @DisplayName("Cannot activate the life-gain ability without a creature to sacrifice")
    void cannotActivateLifeGainAbilityWithoutCreature() {
        addReadyMiren(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose a creature to sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotActivateLifeGainAbilityUsingOpponentsCreature() {
        Permanent miren = addReadyMiren(player1);
        addCreatureReady(player2, new EiganjoFreeRiders());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose a creature to sacrifice");

        assertThat(miren.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    private Permanent addReadyMiren(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new MirenTheMoaningWell());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
