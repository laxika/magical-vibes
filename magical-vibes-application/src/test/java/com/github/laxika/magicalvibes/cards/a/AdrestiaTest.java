package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Adrestia.class, AssassinInitiate.class, AmoeboidChangeling.class, GrizzlyBears.class})
class AdrestiaTest extends BaseCardTest {

    @Test
    @DisplayName("Assassin crewing draws a card and makes Adrestia an Assassin until end of turn")
    void assassinCrewTriggersOnAttack() {
        Permanent adrestia = addAdrestiaReady();
        addCreatureReady(player1, new AssassinInitiate());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        crewAdrestia();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gqs.hasEffectiveSubtype(gd, adrestia, CardSubtype.ASSASSIN)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, adrestia, CardSubtype.ASSASSIN)).isFalse();
    }

    @Test
    @DisplayName("Crewing with a non-Assassin does not trigger Adrestia")
    void nonAssassinCrewDoesNotTrigger() {
        Permanent adrestia = addAdrestiaReady();
        addCreatureReady(player1, new GrizzlyBears());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        crewAdrestia();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
        assertThat(gqs.hasEffectiveSubtype(gd, adrestia, CardSubtype.ASSASSIN)).isFalse();
    }

    @Test
    @DisplayName("Adrestia remembers the Assassin even after it loses its creature types")
    void remembersAssassinThatStopsBeingAssassin() {
        Permanent adrestia = addAdrestiaReady();
        Permanent assassin = addCreatureReady(player1, new AssassinInitiate());

        crewAdrestia();
        addCreatureReady(player1, new AmoeboidChangeling());
        harness.activateAbility(player1, 2, 1, null, assassin.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, assassin)).doesNotContain(CardSubtype.ASSASSIN);

        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gqs.hasEffectiveSubtype(gd, adrestia, CardSubtype.ASSASSIN)).isTrue();
    }

    private Permanent addAdrestiaReady() {
        return addCreatureReady(player1, new Adrestia());
    }

    private void crewAdrestia() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
