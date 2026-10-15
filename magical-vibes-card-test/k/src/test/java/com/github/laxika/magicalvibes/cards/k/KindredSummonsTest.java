package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.c.ContainmentPriest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KindredSummons.class, AvianChangeling.class, ContainmentPriest.class, GrizzlyBears.class, HillGiant.class, PsychogenicProbe.class, Shock.class})
class KindredSummonsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts as many chosen-type creature cards onto the battlefield as matching creatures you control")
    void putsMatchingCreatureCardsOntoBattlefieldUpToControlledCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card nonmatching = new Shock();
        Card firstMatching = new GrizzlyBears();
        Card between = new HillGiant();
        Card secondMatching = new GrizzlyBears();
        Card beyondRequiredCount = new GrizzlyBears();
        harness.setLibrary(player1,
                List.of(nonmatching, firstMatching, between, secondMatching, beyondRequiredCount));

        castAndChooseBear();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(firstMatching.getId(), secondMatching.getId())
                .doesNotContain(beyondRequiredCount.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(nonmatching.getId(), between.getId(), beyondRequiredCount.getId());
    }

    @Test
    @DisplayName("A Changeling you control counts toward the chosen type")
    void changelingCountsTowardChosenType() {
        harness.addToBattlefield(player1, new AvianChangeling());
        Card matching = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Shock(), matching));

        castAndChooseBear();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(matching.getId()));
    }

    @Test
    @DisplayName("Choosing a type you control none of reveals no cards")
    void chosenTypeYouControlNoneOfRevealsNoCards() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        Card shock = new Shock();
        Card giant = new HillGiant();
        harness.setLibrary(player1, List.of(shock, giant));

        castAndChoose("ELF");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(shock.getId(), giant.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(giant.getId()));
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Shuffling the revealed remainder triggers library-shuffle abilities")
    void shufflesRevealedRemainderIntoLibrary() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Shock(), new GrizzlyBears()));

        castAndChooseBear();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("An empty library is still shuffled")
    void emptyLibraryStillTriggersShuffleAbilities() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of());

        castAndChooseBear();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A Changeling in the library matches the chosen type")
    void putsRevealedChangelingOntoBattlefield() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card changeling = new AvianChangeling();
        Card beyond = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Shock(), changeling, beyond));

        castAndChooseBear();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(changeling.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .contains(beyond.getId()).doesNotContain(changeling.getId());
    }

    @Test
    @DisplayName("Too few matches reveals the whole library and puts every match onto the battlefield")
    void putsAllAvailableMatchesOntoBattlefieldWhenLibraryRunsOut() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card matching = new GrizzlyBears();
        Card shock = new Shock();
        Card giant = new HillGiant();
        harness.setLibrary(player1, List.of(shock, matching, giant));

        castAndChooseBear();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(matching.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(shock.getId(), giant.getId());
    }

    @Test
    @DisplayName("Opponents' matching creatures do not increase the reveal count")
    void doesNotCountOpponentsCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        castAndChooseBear();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(second.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(second.getId());
    }

    @Test
    @DisplayName("Containment Priest does not exile creatures entering alongside it")
    void revealedContainmentPriestDoesNotAffectSimultaneousEntrants() {
        harness.addToBattlefield(player1, new AvianChangeling());
        harness.addToBattlefield(player1, new AvianChangeling());
        Card priest = new ContainmentPriest();
        Card changeling = new AvianChangeling();
        harness.setLibrary(player1, List.of(priest, changeling));

        castAndChoose("CLERIC");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(priest.getId(), changeling.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castAndChooseBear() {
        castAndChoose("BEAR");
    }

    private void castAndChoose(String creatureType) {
        harness.setHand(player1, List.of(new KindredSummons()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveInstant(player1, 0);
        harness.handleListChoice(player1, creatureType);
    }
}
