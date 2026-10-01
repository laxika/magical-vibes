package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.r.RiteOfFlame;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThrummingStone.class, BorealDruid.class, RiteOfFlame.class,
        SnowCoveredForest.class, SnowCoveredMountain.class})
class ThrummingStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the controller's spells ripple 4")
    void givesControllerSpellsRipple() {
        prepareSpellCast(player1, List.of(
                new BorealDruid(), new SnowCoveredMountain(),
                new SnowCoveredMountain(), new SnowCoveredMountain()));

        castBorealDruid(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.description()).contains("Boreal Druid");
    }

    @Test
    @DisplayName("Does not give an opponent's spells ripple")
    void doesNotGiveOpponentsSpellsRipple() {
        prepareSpellCast(player2, List.of(new BorealDruid()));

        castBorealDruid(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Boreal Druid");
    }

    @Test
    @DisplayName("Ripple free-casts a matching revealed spell without paying its mana cost")
    void rippleFreeCastsMatchingSpellWithoutPayingMana() {
        prepareSpellCast(player1, List.of(new BorealDruid()));

        castBorealDruid(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Boreal Druid")).isEqualTo(2);
    }

    @Test
    @DisplayName("Gives the controller's noncreature spells ripple 4")
    void givesControllerNoncreatureSpellsRipple() {
        prepareSpellCast(player1, List.of(
                new SnowCoveredMountain(), new SnowCoveredForest(),
                new SnowCoveredMountain(), new SnowCoveredForest()));

        harness.castFromHand(player1, new RiteOfFlame(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Ripple puts all four nonmatching revealed cards on the bottom in the chosen order")
    void ripplePutsFourNonmatchingCardsOnBottom() {
        prepareSpellCast(player1, List.of(
                new SnowCoveredMountain(), new SnowCoveredForest(),
                new SnowCoveredMountain(), new SnowCoveredForest()));

        castBorealDruid(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).hasSize(4);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Snow-Covered Forest", "Snow-Covered Mountain",
                        "Snow-Covered Forest", "Snow-Covered Mountain");
    }

    @Test
    @DisplayName("Ripple can be declined without revealing the library")
    void rippleCanBeDeclinedWithoutRevealingLibrary() {
        List<Card> library = List.of(new BorealDruid(), new SnowCoveredMountain());
        prepareSpellCast(player1, library);

        castBorealDruid(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
    }

    private void prepareSpellCast(Player caster, List<? extends Card> library) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(caster);
        harness.addToBattlefield(player1, new ThrummingStone());
        harness.setLibrary(caster, library);
    }

    private void castBorealDruid(Player caster) {
        harness.castFromHand(caster, new BorealDruid(), "{G}");
    }
}
