package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Calciderm;
import com.github.laxika.magicalvibes.cards.d.DawnCharm;
import com.github.laxika.magicalvibes.cards.h.Harmonize;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Spellshift.class, DawnCharm.class, Harmonize.class, Calciderm.class})
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
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}
