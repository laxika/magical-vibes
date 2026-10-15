package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlteredEgo;
import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.cards.s.SeagrafSkaab;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorruptedGrafstone.class, QuilledWolf.class, SeagrafSkaab.class, AlteredEgo.class})
class CorruptedGrafstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new CorruptedGrafstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent grafstone = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(grafstone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Automatically adds the only color represented in the controller's graveyard")
    void autoAddsSingleGraveyardColor() {
        Permanent grafstone = harness.addToBattlefieldAndReturn(player1, new CorruptedGrafstone());
        grafstone.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new QuilledWolf()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Offers only colors represented by cards in the controller's graveyard")
    void offersColorsFromGraveyard() {
        Permanent grafstone = harness.addToBattlefieldAndReturn(player1, new CorruptedGrafstone());
        grafstone.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new QuilledWolf(), new SeagrafSkaab()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("BLUE", "GREEN");

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Produces no mana when the graveyard has no colored cards")
    void producesNoManaWithoutColoredGraveyardCard() {
        Permanent grafstone = harness.addToBattlefieldAndReturn(player1, new CorruptedGrafstone());
        grafstone.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new CorruptedGrafstone()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An empty graveyard produces no mana but still pays the tap cost")
    void emptyGraveyardStillPaysTapCost() {
        Permanent grafstone = harness.addToBattlefieldAndReturn(player1, new CorruptedGrafstone());
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, 0, null, null);

        assertThat(grafstone.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the controller's graveyard supplies colors")
    void ignoresOpponentsGraveyard() {
        Permanent grafstone = harness.addToBattlefieldAndReturn(player2, new CorruptedGrafstone());
        harness.setGraveyard(player1, List.of(new SeagrafSkaab()));
        harness.setGraveyard(player2, List.of(new QuilledWolf(), new CorruptedGrafstone()));

        harness.activateAbility(player2, 0, null, null);

        assertThat(grafstone.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLUE", "GREEN"})
    @DisplayName("Either color of one multicolored card can produce exactly one mana")
    void choosesEitherColorOfMulticoloredCard(ManaColor color) {
        Permanent grafstone = harness.addToBattlefieldAndReturn(player1, new CorruptedGrafstone());
        harness.setGraveyard(player1, List.of(new AlteredEgo(), new CorruptedGrafstone()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(grafstone.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("BLUE", "GREEN");

        harness.handleListChoice(player1, color.name());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rejects a color absent from the controller's graveyard")
    void rejectsColorNotRepresentedInGraveyard() {
        harness.addToBattlefield(player1, new CorruptedGrafstone());
        harness.setGraveyard(player1, List.of(new AlteredEgo()));

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "RED"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("BLUE", "GREEN");

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A tapped Grafstone cannot activate again")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new CorruptedGrafstone());
        harness.setGraveyard(player1, List.of(new QuilledWolf()));
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }
}
