package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BruvacTheGrandiloquent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AberrantResearcher.class, GrizzlyBears.class, Pyroclasm.class, Shock.class, BruvacTheGrandiloquent.class})
class AberrantResearcherTest extends BaseCardTest {

    @Test
    @DisplayName("Mills instant and transforms into Perfected Form")
    void millsInstantAndTransforms() {
        Permanent researcher = harness.addToBattlefieldAndReturn(player1, new AberrantResearcher());

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
        Permanent researcher = harness.addToBattlefieldAndReturn(player1, new AberrantResearcher());

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
        Permanent researcher = harness.addToBattlefieldAndReturn(player1, new AberrantResearcher());

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
        Permanent researcher = harness.addToBattlefieldAndReturn(player1, new AberrantResearcher());
        gd.playerDecks.get(player1.getId()).clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(researcher.isTransformed()).isFalse();
        assertThat(researcher.getCard().getName()).isEqualTo("Aberrant Researcher");
    }

    @Test
    @DisplayName("Does not trigger on opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        Permanent researcher = harness.addToBattlefieldAndReturn(player1, new AberrantResearcher());

        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);

        advanceToUpkeep(player2);

        assertThat(researcher.isTransformed()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(shock);
    }

    @Test
    @DisplayName("Transforms when Bruvac doubles the mill and only the second card is an instant")
    void transformsWhenSecondCardOfDoubledMillIsInstant() {
        Permanent researcher = harness.addToBattlefieldAndReturn(player1, new AberrantResearcher());
        harness.addToBattlefield(player2, new BruvacTheGrandiloquent());
        Card creature = new AberrantResearcher();
        Card instant = new Shock();
        harness.setLibrary(player1, List.of(creature, instant));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature, instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(researcher.isTransformed()).isTrue();
        assertThat(researcher.getCard().getName()).isEqualTo("Perfected Form");
    }

    @Test
    @DisplayName("Perfected Form does not mill on subsequent upkeeps")
    void transformedFaceDoesNotMillAgain() {
        Permanent researcher = harness.addToBattlefieldAndReturn(player1, new AberrantResearcher());
        harness.setLibrary(player1, List.of(new Shock()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(researcher.isTransformed()).isTrue();

        Card nextCard = new Shock();
        harness.setLibrary(player1, List.of(nextCard));
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(researcher.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Still mills when the researcher dies in response to its upkeep trigger")
    void millsAfterSourceDies() {
        Permanent researcher = harness.addToBattlefieldAndReturn(player1, new AberrantResearcher());
        Card milledCard = new Pyroclasm();
        harness.setLibrary(player1, List.of(milledCard));
        harness.setHand(player1, List.of(new Shock()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, researcher.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(researcher);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milledCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
