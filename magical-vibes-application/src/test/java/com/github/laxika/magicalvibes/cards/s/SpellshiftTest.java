package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Calciderm;
import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.d.DawnCharm;
import com.github.laxika.magicalvibes.cards.h.Harmonize;
import com.github.laxika.magicalvibes.cards.p.Pongify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Spellshift.class, DawnCharm.class, Harmonize.class, Calciderm.class,
        Pongify.class, CosisTrickster.class})
class SpellshiftTest extends BaseCardTest {

    @Test
    void decliningTheFreeCastCountersTheTargetAndShufflesTheRevealedCards() {
        DawnCharm target = new DawnCharm();
        Calciderm nonmatching = new Calciderm();
        Harmonize found = new Harmonize();
        castSpellshift(target, List.of(nonmatching, found));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(target.getId()));
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Harmonize");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Dawn Charm");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(nonmatching, found);
    }

    @Test
    void acceptingTheFreeCastCastsTheFoundSpellAndShufflesTheOtherRevealedCards() {
        DawnCharm target = new DawnCharm();
        Calciderm nonmatching = new Calciderm();
        Harmonize found = new Harmonize();
        castSpellshift(target, List.of(nonmatching, found));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(found);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        harness.assertInGraveyard(player1, "Dawn Charm");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatching);
    }

    @Test
    void noInstantOrSorceryIsFoundAndTheTargetIsStillCountered() {
        DawnCharm target = new DawnCharm();
        Calciderm nonmatching = new Calciderm();
        castSpellshift(target, List.of(nonmatching));

        harness.assertInGraveyard(player1, "Dawn Charm");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatching);
    }

    @Test
    void emptyLibraryStillCountersTheTarget() {
        DawnCharm target = new DawnCharm();
        castSpellshift(target, List.of());

        harness.assertInGraveyard(player1, "Dawn Charm");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void revealStopsAtTheFirstInstantEvenWhenItCannotBeCast() {
        DawnCharm target = new DawnCharm();
        Calciderm nonmatching = new Calciderm();
        Pongify found = new Pongify();
        Harmonize unrevealed = new Harmonize();
        harness.addToBattlefield(player1, new Calciderm());
        castSpellshift(target, List.of(nonmatching, found, unrevealed));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(found);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Dawn Charm");
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(nonmatching, found, unrevealed);
    }

    @Test
    @CardUsed({Spellshift.class, DawnCharm.class, Pongify.class, CosisTrickster.class})
    void shuffleWaitsUntilTheRevealedSpellHasBeenCast() {
        Permanent trickster = harness.addToBattlefieldAndReturn(player2, new CosisTrickster());
        Pongify found = new Pongify();
        castSpellshift(new DawnCharm(), List.of(found));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId()
                .equals(trickster.getCard().getId()));

        harness.handlePermanentChosen(player1, trickster.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(found);
        assertThat(gd.stack.getLast().getCard()).isSameAs(trickster.getCard());
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @CardUsed({Spellshift.class, DawnCharm.class, CosisTrickster.class})
    void emptyLibraryStillShufflesAndTriggersShuffleAbilities() {
        CosisTrickster trickster = new CosisTrickster();
        harness.addToBattlefield(player2, trickster);
        castSpellshift(new DawnCharm(), List.of());

        harness.assertInGraveyard(player1, "Dawn Charm");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(trickster);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    void canCounterASorceryAndCastFromItsControllersLibrary() {
        Harmonize target = new Harmonize();
        Harmonize found = new Harmonize();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(found));
        harness.castFromHand(player1, target, "{2}{G}{G}");
        harness.setHand(player2, List.of(new Spellshift()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Harmonize");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(found);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void revealedCounterspellCanTargetAnotherSpellStillOnTheStack() {
        DawnCharm remaining = new DawnCharm();
        DawnCharm target = new DawnCharm();
        Spellshift found = new Spellshift();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(remaining, target));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(found));
        harness.castModalInstant(player1, 0, 0, List.of());
        harness.castModalInstant(player1, 0, 0, List.of());
        harness.setHand(player2, List.of(new Spellshift()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, remaining.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard()).isSameAs(found);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(remaining.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotTargetACreatureSpell() {
        Calciderm target = new Calciderm();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, target, "{2}{W}{W}");
        harness.setHand(player2, List.of(new Spellshift()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(target);
        harness.assertInHand(player2, "Spellshift");
    }

    private void castSpellshift(DawnCharm target, List<Card> library) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, library);

        harness.setHand(player2, List.of(new Spellshift()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }
}
