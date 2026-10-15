package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShimmeringMirage.class, Forest.class, GrizzlyBears.class, YavimayaCoast.class})
class ShimmeringMirageTest extends BaseCardTest {

    @Test
    @DisplayName("Target land becomes the chosen basic land type and you draw a card")
    void changesTargetLandAndDraws() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ShimmeringMirage()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, forest.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "ISLAND");

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The transformed land taps for the chosen basic land type")
    void transformedLandProducesChosenMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ShimmeringMirage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, forest.getId());
        harness.handleListChoice(player1, "ISLAND");

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isOne();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("The chosen land type wears off at end of turn")
    void chosenTypeWearsOffAtEndOfTurn() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ShimmeringMirage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, forest.getId());
        harness.handleListChoice(player1, "ISLAND");

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    @DisplayName("Cannot target a non-land permanent")
    void cannotTargetNonLand() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShimmeringMirage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");
    }

    @ParameterizedTest
    @EnumSource(value = CardSubtype.class, names = {"PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST"})
    @DisplayName("All five basic land types can be chosen, and drawing waits for the choice")
    void canChooseEveryBasicLandType(CardSubtype chosenType) {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new YavimayaCoast());
        harness.setHand(player1, List.of(new ShimmeringMirage()));
        harness.setLibrary(player1, List.of(new ShimmeringMirage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, land.getId());

        harness.assertNotInHand(player1, "Shimmering Mirage");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.handleListChoice(player1, chosenType.name());

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(chosenType);
        harness.assertInHand(player1, "Shimmering Mirage");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A transformed nonbasic land produces its new mana without its printed damage ability")
    void nonbasicLandProducesChosenManaWithoutDamage() {
        harness.addToBattlefield(player1, new YavimayaCoast());
        harness.setHand(player1, List.of(new ShimmeringMirage()));
        harness.setLibrary(player1, List.of(new ShimmeringMirage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Yavimaya Coast"));
        harness.handleListChoice(player1, "SWAMP");
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isOne();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("No card is drawn when the only target leaves the battlefield before resolution")
    void doesNotDrawWhenTargetLeavesBattlefield() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new YavimayaCoast());
        harness.setHand(player1, List.of(new ShimmeringMirage()));
        harness.setLibrary(player1, List.of(new ShimmeringMirage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, land.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, land);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotInHand(player1, "Shimmering Mirage");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Shimmering Mirage");
        harness.assertInHand(player2, "Yavimaya Coast");
    }
}
