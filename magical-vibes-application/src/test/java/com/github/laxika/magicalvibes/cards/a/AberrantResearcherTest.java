package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AberrantResearcherTest extends BaseCardTest {

    @Test
    @DisplayName("Mills instant and transforms into Perfected Form")
    void millsInstantAndTransforms() {
        harness.addToBattlefield(player1, new AberrantResearcher());
        Permanent researcher = findPermanent(player1, "Aberrant Researcher");

        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
        assertThat(researcher.isTransformed()).isTrue();
        assertThat(researcher.getCard().getName()).isEqualTo("Perfected Form");
        assertThat(gqs.getEffectivePower(gd, researcher)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, researcher)).isEqualTo(4);
    }

    @Test
    @DisplayName("Mills sorcery and transforms into Perfected Form")
    void millsSorceryAndTransforms() {
        harness.addToBattlefield(player1, new AberrantResearcher());
        Permanent researcher = findPermanent(player1, "Aberrant Researcher");

        Card pyroclasm = new Pyroclasm();
        gd.playerDecks.get(player1.getId()).addFirst(pyroclasm);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(researcher.isTransformed()).isTrue();
        assertThat(researcher.getCard().getName()).isEqualTo("Perfected Form");
    }

    @Test
    @DisplayName("Mills creature without transforming")
    void millsCreatureWithoutTransforming() {
        harness.addToBattlefield(player1, new AberrantResearcher());
        Permanent researcher = findPermanent(player1, "Aberrant Researcher");

        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(researcher.isTransformed()).isFalse();
        assertThat(researcher.getCard().getName()).isEqualTo("Aberrant Researcher");
    }

    @Test
    @DisplayName("Does nothing when library is empty")
    void doesNothingWhenLibraryEmpty() {
        harness.addToBattlefield(player1, new AberrantResearcher());
        Permanent researcher = findPermanent(player1, "Aberrant Researcher");
        gd.playerDecks.get(player1.getId()).clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(researcher.isTransformed()).isFalse();
        assertThat(researcher.getCard().getName()).isEqualTo("Aberrant Researcher");
    }

    @Test
    @DisplayName("Does not trigger on opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new AberrantResearcher());
        Permanent researcher = findPermanent(player1, "Aberrant Researcher");

        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);

        advanceToUpkeep(player2);

        assertThat(researcher.isTransformed()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(shock);
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
