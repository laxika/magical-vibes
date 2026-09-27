package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({KindredSummons.class, AvianChangeling.class, Forest.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class, PsychogenicProbe.class, Shock.class})
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
        Card shock = new Shock();
        Card giant = new HillGiant();
        harness.setLibrary(player1, List.of(shock, giant));

        castAndChoose("ELF");

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(shock.getId(), giant.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(giant.getId()));
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

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    private void castAndChooseBear() {
        castAndChoose("BEAR");
    }

    private void castAndChoose(String creatureType) {
        harness.setHand(player1, List.of(new KindredSummons()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, creatureType);
    }
}
