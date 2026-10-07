package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurvivorsEncampment.class, FeralProwler.class, SylvanAwakening.class})
class SurvivorsEncampmentTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C}")
    void tapAddsColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SurvivorsEncampment());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{T}, Tap an untapped creature: Add one mana of any color")
    void tapsCreatureAndAddsChosenColorMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SurvivorsEncampment());
        Permanent creature = addCreatureReady(player1, new FeralProwler());

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
        harness.addToBattlefield(player1, new SurvivorsEncampment());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void canTapSummoningSickCreatureForEachColor(ManaColor color) {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SurvivorsEncampment());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FeralProwler());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(land.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotTapAlreadyTappedCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SurvivorsEncampment());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FeralProwler());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTapOpponentsCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SurvivorsEncampment());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FeralProwler());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(land.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void choosesWhichControlledCreatureToTap() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SurvivorsEncampment());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FeralProwler());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new FeralProwler());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, second.getId());
        harness.handleListChoice(player1, "BLUE");

        assertThat(land.isTapped()).isTrue();
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"COLORLESS", "GREEN"})
    void tappedLandCannotActivateEitherAbility(ManaColor color) {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SurvivorsEncampment());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FeralProwler());
        land.tap();
        int abilityIndex = color == ManaColor.COLORLESS ? 0 : 1;

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
    }

    @Test
    void animatedEncampmentCannotPayBothTapCostsByItself() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SurvivorsEncampment());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SylvanAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(land.isTapped()).isFalse();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
