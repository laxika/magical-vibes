package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZimonesHypothesis.class, GrizzlyBears.class, LlanowarElves.class, Ornithopter.class})
class ZimonesHypothesisTest extends BaseCardTest {

    private void castHypothesis() {
        harness.setHand(player1, List.of(new ZimonesHypothesis()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0);
    }

    @Test
    @DisplayName("The counter is optional and the spell then asks for odd or even")
    void counterIsOptional() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        castHypothesis();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "EVEN");

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(elf);
    }

    @Test
    @DisplayName("The chosen creature may be controlled by an opponent and its new power is used")
    void counterChangesPowerParityBeforeBounce() {
        Permanent ownOdd = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opponentEven = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castHypothesis();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentEven.getId()));
        harness.handleListChoice(player1, "ODD");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownOdd);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentEven);
        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing even returns zero and positive even power creatures on both battlefields")
    void evenIncludesZeroPower() {
        Permanent zero = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent even = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent odd = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castHypothesis();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleListChoice(player1, "EVEN");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(zero);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(odd);
        harness.assertInHand(player1, "Ornithopter");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(even);
    }

    @Test
    @DisplayName("Accepting the counter on the only creature changes its parity and can leave it in play")
    void counteredCreatureCanRemain() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        castHypothesis();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "ODD");

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(elf);
        assertThat(elf.getPlusOnePlusOneCounters()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Zimone's Hypothesis");
    }

    @Test
    @DisplayName("An empty battlefield does not prevent choosing parity or completing resolution")
    void resolvesWithoutCreatures() {
        castHypothesis();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        harness.handleListChoice(player1, "EVEN");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Zimone's Hypothesis");
    }
}
