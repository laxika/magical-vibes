package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.Antagonize;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Strangle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnluckyWitness.class, Forest.class, Strangle.class, Antagonize.class})
class UnluckyWitnessTest extends BaseCardTest {

    @Test
    void deathTriggerExilesTwoWithoutRequiringAnImmediateChoice() {
        Card first = new Forest();
        Card second = new UnluckyWitness();
        Card third = new Forest();

        killWitnessWithLibrary(List.of(first, second, third));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Unlucky Witness");
    }

    @Test
    void mayPlayFirstCardAsALandButCannotThenPlaySecondCard() {
        Card land = new Forest();
        Card spell = new UnluckyWitness();
        killWitnessWithLibrary(List.of(land, spell));

        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Forest");
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
    }

    @Test
    void mayPlaySecondCardAsASpellButCannotThenPlayFirstCard() {
        Card land = new Forest();
        Card spell = new UnluckyWitness();
        killWitnessWithLibrary(List.of(land, spell));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Unlucky Witness");
        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land);
    }

    @Test
    void emptyLibraryDoesNotRequireAChoiceOrCauseADrawLoss() {
        killWitnessWithLibrary(List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Unlucky Witness");
    }

    @Test
    void oneCardLibraryAllowsPlayingTheOnlyCard() {
        Card land = new Forest();
        killWitnessWithLibrary(List.of(land));

        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void permissionEndsWhenNextEndStepBeginsRatherThanDuringCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new UnluckyWitness());
        Card spell = new Antagonize();
        killWitnessWithLibrary(List.of(spell));
        completeLegacyChoiceIfPresent(spell);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
    }

    @Test
    void castingExiledSpellStillRequiresItsManaCost() {
        Card spell = new UnluckyWitness();
        killWitnessWithLibrary(List.of(spell));
        completeLegacyChoiceIfPresent(spell);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Unlucky Witness");
    }

    private void killWitnessWithLibrary(List<Card> library) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent witness = harness.addToBattlefieldAndReturn(player1, new UnluckyWitness());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new Strangle()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, witness.getId());
        resolveAllTriggers();
    }

    private void completeLegacyChoiceIfPresent(Card card) {
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.ExiledCardMayPlayChoice) {
            harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        }
    }
}
