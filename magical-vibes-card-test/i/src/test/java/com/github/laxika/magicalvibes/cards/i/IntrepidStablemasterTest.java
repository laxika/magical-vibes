package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.cards.l.LuxuriousLocomotive;
import com.github.laxika.magicalvibes.cards.t.TrainedArynx;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntrepidStablemaster.class, LuxuriousLocomotive.class, TrainedArynx.class})
class IntrepidStablemasterTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one green mana")
    void addsGreenMana() {
        Permanent stablemaster = addStablemaster();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(stablemaster.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second ability adds two mana of the chosen color for Mount or Vehicle spells")
    void addsMountOrVehicleSpellOnlyMana() {
        addStablemaster();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        Card vehicle = new LuxuriousLocomotive();
        harness.setHand(player1, List.of(vehicle));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The restricted mana cannot cast a non-Mount, non-Vehicle spell")
    void restrictedManaCannotCastOtherSpells() {
        addStablemaster();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        Card creature = new IntrepidStablemaster();
        harness.setHand(player1, List.of(creature));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    void restrictedWhiteManaPaysForMountsColoredAndGenericCosts() {
        addStablemaster();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.setHand(player1, List.of(new TrainedArynx()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void restrictedManaDoesNotIgnoreMountsColoredCost() {
        addStablemaster();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");
        harness.setHand(player1, List.of(new TrainedArynx()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void secondAbilityAddsTwoManaOfExactlyOneChosenColor(ManaColor color) {
        Permanent stablemaster = addStablemaster();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(stablemaster.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOnlyManaForColor(Set.of(CardSubtype.MOUNT), color)).isEqualTo(2);
        harness.setHand(player1, List.of(new LuxuriousLocomotive()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void firstAbilityProducesUnrestrictedMana() {
        addStablemaster();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new IntrepidStablemaster()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void summoningSicknessPreventsEitherTapAbility(int abilityIndex) {
        harness.addToBattlefield(player1, new IntrepidStablemaster());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void tappedStablemasterCannotActivateEitherAbility(int abilityIndex) {
        Permanent stablemaster = addStablemaster();
        stablemaster.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    private Permanent addStablemaster() {
        return addCreatureReady(player1, new IntrepidStablemaster());
    }
}
