package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardSubtype;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FountainOfIchor.class})
class FountainOfIchorTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Fountain of Ichor adds one mana of the chosen color")
    void tapsForAnyColor() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfIchor());

        harness.activateAbility(player1, 0, null, null);

        assertThat(fountain.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying three mana makes Fountain of Ichor a 3/3 Dinosaur artifact creature")
    void animatesIntoDinosaur() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfIchor());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, fountain)).isTrue();
        assertThat(gqs.isArtifact(fountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, fountain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, fountain)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, fountain, CardSubtype.DINOSAUR)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, fountain)).isFalse();
        assertThat(gqs.isArtifact(fountain)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, fountain, CardSubtype.DINOSAUR)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED"})
    @DisplayName("Fountain of Ichor can produce each other mana color")
    void tapsForOtherColors(ManaColor color) {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfIchor());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(fountain.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Fountain of Ichor can animate without untapping")
    void tappedFountainCanAnimate() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfIchor());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, fountain)).isFalse();
        harness.passBothPriorities();

        assertThat(fountain.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, fountain)).isTrue();
        assertThat(gqs.getEffectivePower(gd, fountain)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("An animated Fountain controlled since the turn began retains its mana ability")
    void animatedFountainCanProduceMana() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfIchor());
        fountain.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(fountain.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, fountain)).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A newly controlled Fountain cannot pay its tap cost while animated")
    void animatedNewFountainHasSummoningSickness() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfIchor());
        fountain.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(fountain.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.isCreature(gd, fountain)).isFalse();
        assertThat(fountain.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }
}
