package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AllosaurusShepherd.class, Counterspell.class, GrizzlyBears.class,
        LlanowarElves.class, Shock.class})
class AllosaurusShepherdTest extends BaseCardTest {

    @Test
    @DisplayName("The Shepherd spell cannot be countered")
    void spellCannotBeCountered() {
        AllosaurusShepherd shepherd = new AllosaurusShepherd();
        harness.setHand(player1, List.of(shepherd));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shepherd.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Allosaurus Shepherd");
        harness.assertInGraveyard(player2, "Counterspell");
    }

    @Test
    @DisplayName("The Shepherd protects green spells but not other colors")
    void protectsGreenSpellsOnly() {
        harness.addToBattlefield(player1, new AllosaurusShepherd());
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Counterspell");

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertOnBattlefield(player1, "Allosaurus Shepherd");
    }

    @Test
    @DisplayName("Ability sets only Elves you control to 5/5 Dinosaurs until end of turn")
    void transformsOwnElvesUntilEndOfTurn() {
        Permanent shepherd = addCreatureReady(player1, new AllosaurusShepherd());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentElves = addCreatureReady(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shepherd)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, shepherd)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, shepherd)).contains(CardSubtype.DINOSAUR);
        assertThat(gqs.effectiveCreatureSubtypes(gd, elves)).contains(CardSubtype.DINOSAUR);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).doesNotContain(CardSubtype.DINOSAUR);
        assertThat(gqs.getEffectivePower(gd, opponentElves)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opponentElves)).doesNotContain(CardSubtype.DINOSAUR);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shepherd)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, elves)).doesNotContain(CardSubtype.DINOSAUR);
    }
}
