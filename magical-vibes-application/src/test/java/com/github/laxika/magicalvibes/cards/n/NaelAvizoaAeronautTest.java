package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NaelAvizoaAeronaut.class, GrizzlyBears.class, Plains.class, Island.class, Swamp.class,
        Mountain.class, Forest.class})
class NaelAvizoaAeronautTest extends BaseCardTest {

    @Test
    @DisplayName("Five basic land types draw after the top-card choice")
    void fiveBasicLandTypesDrawAfterTopCardChoice() {
        addAllBasicLandTypes();
        Card first = new GrizzlyBears();
        Card chosen = new GrizzlyBears();
        Card last = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, chosen, last));
        addAttackingNael();

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(first, chosen, last);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, last);
    }

    @Test
    @DisplayName("Fewer than five basic land types still look at cards but do not draw")
    void fewerThanFiveBasicLandTypesDoNotDraw() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        Card chosen = new GrizzlyBears();
        Card last = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosen, last));
        addAttackingNael();

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(chosen, last);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(chosen, last);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(chosen);
    }

    @Test
    @DisplayName("Duplicate basic land types count only once")
    void duplicateBasicLandTypesCountOnlyOnce() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card outsideDomain = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, outsideDomain));
        addAttackingNael();

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(first, second);
        assertThat(search.params().cards()).doesNotContain(third, outsideDomain);
    }

    private void addAllBasicLandTypes() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
    }

    private Permanent addAttackingNael() {
        Permanent nael = addCreatureReady(player1, new NaelAvizoaAeronaut());
        nael.setAttacking(true);
        return nael;
    }
}
