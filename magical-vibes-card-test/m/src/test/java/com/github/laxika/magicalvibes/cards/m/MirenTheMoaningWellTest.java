package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EiganjoFreeRiders;
import com.github.laxika.magicalvibes.model.CounterType;
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

    @Test
    @DisplayName("A newly played Miren can tap for mana immediately")
    void newlyPlayedMirenCanTapForMana() {
        Permanent miren = harness.addToBattlefieldAndReturn(player1, new MirenTheMoaningWell());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(miren.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The sole creature's modified toughness is captured when paying the cost")
    void gainsLifeEqualToModifiedToughness() {
        addReadyMiren(player1);
        Permanent creature = addCreatureReady(player1, new EiganjoFreeRiders());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Eiganjo Free-Riders");
        harness.assertInGraveyard(player1, "Eiganjo Free-Riders");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 6);
    }

    @Test
    @DisplayName("Choosing between creatures uses the chosen creature's modified toughness")
    void chosenCreatureDeterminesLifeGain() {
        addReadyMiren(player1);
        Permanent remaining = addCreatureReady(player1, new EiganjoFreeRiders());
        Permanent chosen = addCreatureReady(player1, new EiganjoFreeRiders());
        chosen.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(remaining).doesNotContain(chosen);
        harness.assertInGraveyard(player1, "Eiganjo Free-Riders");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("A tapped creature with summoning sickness can be sacrificed")
    void canSacrificeTappedSummoningSickCreature() {
        addReadyMiren(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EiganjoFreeRiders());
        creature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Eiganjo Free-Riders");
        harness.assertInGraveyard(player1, "Eiganjo Free-Riders");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    private Permanent addReadyMiren(Player player) {
        return addCreatureReady(player, new MirenTheMoaningWell());
    }
}
