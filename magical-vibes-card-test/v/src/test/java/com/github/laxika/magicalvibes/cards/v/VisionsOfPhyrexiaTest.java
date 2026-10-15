package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VisionsOfPhyrexia.class, Forest.class, ArgothianSprite.class, GiantGrowth.class})
class VisionsOfPhyrexiaTest extends BaseCardTest {

    @Test
    @DisplayName("At your upkeep, it exiles the top card and lets you play it")
    void upkeepExilesTopCardAndLetsYouPlayIt() {
        Card topCard = new Forest();
        harness.addToBattlefield(player1, new VisionsOfPhyrexia());
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, topCard.getId());

        assertThat(findPermanents(player1, "Forest")).hasSize(1);
    }

    @Test
    @DisplayName("Creates a tapped Powerstone at your end step when no card was played from exile")
    void createsPowerstoneWhenNoCardWasPlayedFromExile() {
        harness.addToBattlefield(player1, new VisionsOfPhyrexia());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        List<Permanent> powerstones = findPermanents(player1, "Powerstone");
        assertThat(powerstones).hasSize(1);
        assertThat(powerstones.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not create a Powerstone when you play the exiled card this turn")
    void doesNotCreatePowerstoneAfterPlayingExiledCard() {
        Card topCard = new Forest();
        harness.addToBattlefield(player1, new VisionsOfPhyrexia());
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, topCard.getId());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
    }

    @Test
    void unplayedCardStaysInExileAndStillCreatesPowerstone() {
        Card topCard = new Forest();
        Card nextCard = new Forest();
        harness.addToBattlefield(player1, new VisionsOfPhyrexia());
        harness.setLibrary(player1, List.of(topCard, nextCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
    }

    @Test
    void emptyLibraryStillAllowsEndStepPowerstone() {
        harness.addToBattlefield(player1, new VisionsOfPhyrexia());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
    }

    @Test
    void neitherAbilityTriggersDuringOpponentsTurn() {
        Card topCard = new Forest();
        harness.addToBattlefield(player1, new VisionsOfPhyrexia());
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        advanceToEndStep(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
        assertThat(findPermanents(player2, "Powerstone")).isEmpty();
    }

    @Test
    void exiledSpellRequiresManaAndCastingItPreventsPowerstone() {
        Card topCard = new ArgothianSprite();
        harness.addToBattlefield(player1, new VisionsOfPhyrexia());
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Argothian Sprite");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
    }

    @Test
    void playPermissionDoesNotOverrideCreatureTiming() {
        Card topCard = new ArgothianSprite();
        harness.addToBattlefield(player1, new VisionsOfPhyrexia());
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Argothian Sprite");
    }

    @Test
    void twoCopiesDoNotGrantAnAdditionalLandPlay() {
        Card first = new Forest();
        Card second = new Forest();
        harness.addToBattlefield(player1, new VisionsOfPhyrexia());
        harness.addToBattlefield(player1, new VisionsOfPhyrexia());
        harness.setLibrary(player1, List.of(first, second));

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, first.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Forest")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
    }

    @Test
    void upkeepAbilityAndPlayPermissionSurviveSourceLeavingBattlefield() {
        Card topCard = new Forest();
        Permanent visions = harness.addToBattlefieldAndReturn(player1, new VisionsOfPhyrexia());
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(visions);
        gd.playerGraveyards.get(player1.getId()).add(visions.getCard());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void castingFromExileInResponseToEndStepTriggerPreventsPowerstone() {
        Card topCard = new GiantGrowth();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.addToBattlefield(player1, new VisionsOfPhyrexia());
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, topCard.getId(), creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
    }

    @Test
    void unplayedCardCannotBePlayedOnALaterTurn() {
        Card topCard = new ArgothianSprite();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new VisionsOfPhyrexia());
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToEndStep(player1);
        resolveAllTriggers();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
