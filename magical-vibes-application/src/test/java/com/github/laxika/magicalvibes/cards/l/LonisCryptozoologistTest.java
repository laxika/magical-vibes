package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.c.Chatterstorm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LonisCryptozoologist.class, GrizzlyBears.class, HillGiant.class,
        Chatterstorm.class, Forest.class, Shock.class})
class LonisCryptozoologistTest extends BaseCardTest {

    @Test
    void investigatesWhenAnotherNontokenCreatureEnters() {
        harness.addToBattlefield(player1, new LonisCryptozoologist());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void sacrificesCluesToTakeAnEligiblePermanentFromTargetLibrary() {
        Permanent lonis = addCreatureReady(player1, new LonisCryptozoologist());
        Permanent sacrificedOne = addClueToken();
        Permanent sacrificedTwo = addClueToken();
        GrizzlyBears eligible = new GrizzlyBears();
        HillGiant tooExpensive = new HillGiant();
        harness.setLibrary(player2, List.of(eligible, tooExpensive));
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lonis),
                0, 2, player2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(sacrificedOne, sacrificedTwo);

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(eligible);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(tooExpensive);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotInvestigateForItsOwnEntry() {
        harness.setHand(player1, List.of(new LonisCryptozoologist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Lonis, Cryptozoologist")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void doesNotInvestigateForAnOpponentsCreature() {
        harness.addToBattlefield(player1, new LonisCryptozoologist());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void zeroCluesCanBeSacrificedWithoutChangingTheLibrary() {
        Permanent lonis = addCreatureReady(player1, new LonisCryptozoologist());
        GrizzlyBears top = new GrizzlyBears();
        HillGiant bottom = new HillGiant();
        harness.setLibrary(player2, List.of(top, bottom));

        harness.activateAbility(player1, 0, 0, player2.getId());
        resolveAllTriggers();

        assertThat(lonis.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, bottom);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayDeclineAndPutsOnlyRevealedCardsOnTheBottom() {
        addCreatureReady(player1, new LonisCryptozoologist());
        addClueToken();
        addClueToken();
        GrizzlyBears eligible = new GrizzlyBears();
        HillGiant expensive = new HillGiant();
        GrizzlyBears untouched = new GrizzlyBears();
        harness.setLibrary(player2, List.of(eligible, expensive, untouched));

        harness.activateAbility(player1, 0, 2, player2.getId());
        resolveAllTriggers();
        harness.handleCardChosen(player1, -1);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player2.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(eligible, expensive);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void manaValueLimitUsesChosenXEvenWhenLibraryHasFewerCards() {
        addCreatureReady(player1, new LonisCryptozoologist());
        addClueToken();
        addClueToken();
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setLibrary(player2, List.of(eligible));

        harness.activateAbility(player1, 0, 2, player2.getId());
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void ineligibleCardsGoToTheBottomWithoutAChoice() {
        addCreatureReady(player1, new LonisCryptozoologist());
        addClueToken();
        HillGiant expensive = new HillGiant();
        GrizzlyBears untouched = new GrizzlyBears();
        harness.setLibrary(player2, List.of(expensive, untouched));

        harness.activateAbility(player1, 0, 1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(untouched, expensive);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Hill Giant")).isEmpty();
    }

    @Test
    void doesNotInvestigateWhenCreatureTokensEnter() {
        harness.addToBattlefield(player1, new LonisCryptozoologist());
        harness.setHand(player1, List.of(new Chatterstorm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Squirrel")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void excludesLandsAndNonpermanentSpellsEvenWithinManaValueLimit() {
        addCreatureReady(player1, new LonisCryptozoologist());
        addClueToken();
        addClueToken();
        addClueToken();
        Forest land = new Forest();
        Shock instant = new Shock();
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land, instant, eligible));

        harness.activateAbility(player1, 0, 3, player2.getId());
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(eligible);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(land, instant);
    }

    @Test
    void cannotTargetItsController() {
        Permanent lonis = addCreatureReady(player1, new LonisCryptozoologist());
        Permanent clue = addClueToken();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, player1.getId()))
                .isInstanceOf(RuntimeException.class);

        assertThat(lonis.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(clue);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryStillCostsTheChosenClues() {
        Permanent lonis = addCreatureReady(player1, new LonisCryptozoologist());
        addClueToken();
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, 1, player2.getId());
        resolveAllTriggers();

        assertThat(lonis.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addClueToken() {
        Card clueCard = new Card();
        clueCard.setName("Clue");
        clueCard.setType(CardType.ARTIFACT);
        clueCard.setManaCost("");
        clueCard.setToken(true);
        clueCard.setSubtypes(List.of(CardSubtype.CLUE));
        clueCard.addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{2}, Sacrifice this token: Draw a card."
        ));
        Permanent clue = new Permanent(clueCard);
        clue.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(clue);
        return clue;
    }
}
