package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VitosInquisitor.class, GrizzlyBears.class, Spellbook.class})
class VitosInquisitorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a counter on Vito's Inquisitor and grants menace")
    void sacrificingAnotherCreaturePutsCounterAndGrantsMenace() {
        Permanent inquisitor = addCreatureReady(player1, new VitosInquisitor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(inquisitor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.MENACE)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing another artifact puts a counter on Vito's Inquisitor")
    void sacrificingAnotherArtifactPutsCounter() {
        Permanent inquisitor = addCreatureReady(player1, new VitosInquisitor());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(inquisitor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.MENACE)).isTrue();
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Menace wears off at the end of the turn while the counter remains")
    void menaceWearsOffAtEndOfTurn() {
        Permanent inquisitor = addCreatureReady(player1, new VitosInquisitor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(inquisitor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, inquisitor, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The activated ability cannot sacrifice Vito's Inquisitor itself")
    void activatedAbilityRequiresAnotherPermanent() {
        addCreatureReady(player1, new VitosInquisitor());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
