package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.ExpeditionRaptor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HoldoutSettlement.class, ExpeditionRaptor.class})
class HoldoutSettlementTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C}")
    void tapAddsColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new HoldoutSettlement());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{T}, Tap an untapped creature: Add one mana of any color")
    void tapsCreatureAndAddsChosenColorMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new HoldoutSettlement());
        Permanent creature = addCreatureReady(player1, new ExpeditionRaptor());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate creature-tap ability without an untapped creature")
    void cannotActivateWithoutUntappedCreature() {
        harness.addToBattlefield(player1, new HoldoutSettlement());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void canTapSummoningSickCreatureForEachColor(ManaColor color) {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new HoldoutSettlement());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ExpeditionRaptor());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(land.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTapAlreadyTappedCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new HoldoutSettlement());
        Permanent creature = addCreatureReady(player1, new ExpeditionRaptor());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTapOpponentsCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new HoldoutSettlement());
        Permanent creature = addCreatureReady(player2, new ExpeditionRaptor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void choosesWhichCreatureToTapWhenSeveralAreAvailable() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new HoldoutSettlement());
        Permanent first = addCreatureReady(player1, new ExpeditionRaptor());
        Permanent second = addCreatureReady(player1, new ExpeditionRaptor());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, second.getId());
        harness.handleListChoice(player1, "BLUE");

        assertThat(land.isTapped()).isTrue();
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateEitherAbilityWhenLandIsTapped() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new HoldoutSettlement());
        Permanent creature = addCreatureReady(player1, new ExpeditionRaptor());
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
