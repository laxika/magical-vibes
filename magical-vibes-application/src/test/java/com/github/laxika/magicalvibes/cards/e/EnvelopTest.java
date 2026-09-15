package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.q.QuietSpeculation;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Envelop.class, QuietSpeculation.class, SuntailHawk.class})
class EnvelopTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a sorcery spell")
    void castingTargetsSorcerySpell() {
        QuietSpeculation quietSpeculation = new QuietSpeculation();
        harness.setHand(player1, List.of(quietSpeculation));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new Envelop()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, quietSpeculation.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry envelopEntry = gd.stack.getLast();
        assertThat(envelopEntry.getTargetId()).isEqualTo(quietSpeculation.getId());
    }

    @Test
    @DisplayName("Resolving counters the sorcery spell")
    void countersSorcerySpell() {
        QuietSpeculation quietSpeculation = new QuietSpeculation();
        harness.setHand(player1, List.of(quietSpeculation));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new Envelop()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, quietSpeculation.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Quiet Speculation");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fizzles if the targeted sorcery leaves the stack before resolution")
    void fizzlesIfSorceryLeavesStack() {
        QuietSpeculation quietSpeculation = new QuietSpeculation();
        harness.setHand(player1, List.of(quietSpeculation));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new Envelop()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, quietSpeculation.getId());
        harness.getGameData().stack.removeIf(entry -> entry.getCard().getId().equals(quietSpeculation.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Envelop");
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-sorcery spell")
    void cannotTargetNonSorcerySpell() {
        SuntailHawk hawk = new SuntailHawk();
        harness.setHand(player1, List.of(hawk));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player2, List.of(new Envelop()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, hawk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
