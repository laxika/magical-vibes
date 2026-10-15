package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrystalGrotto.class, Forest.class})
class CrystalGrottoTest extends BaseCardTest {

    @Test
    void entersWithScryOneTrigger() {
        harness.setHand(player1, List.of(new CrystalGrotto()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    void tapsForColorlessMana() {
        Permanent grotto = addReadyGrotto();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(grotto.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void paysOneAndTapsForAnyColorMana() {
        Permanent grotto = addReadyGrotto();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(grotto.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void filtersColoredManaIntoAnyColor(ManaColor color) {
        Permanent grotto = addReadyGrotto();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(grotto.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN))
                .isEqualTo(color == ManaColor.GREEN ? 1 : 0);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void scryCanKeepOrBottomTheTopCard(boolean keepOnTop) {
        Forest top = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(top, second));
        harness.setHand(player1, List.of(new CrystalGrotto()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(
                keepOnTop ? List.of(0) : List.of(), keepOnTop ? List.of() : List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyElementsOf(keepOnTop ? List.of(top, second) : List.of(second, top));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scryWithEmptyLibraryFinishesWithoutAChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new CrystalGrotto()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void canTapForManaImmediatelyAfterEnteringWhileScryIsOnStack() {
        harness.setHand(player1, List.of(new CrystalGrotto()));
        harness.playLand(player1, 0);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotUseBothManaAbilitiesWithoutUntapping() {
        addReadyGrotto();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyGrotto() {
        return harness.addToBattlefieldAndReturn(player1, new CrystalGrotto());
    }
}
