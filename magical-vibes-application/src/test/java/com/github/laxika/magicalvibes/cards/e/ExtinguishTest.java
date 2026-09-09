package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.SleightOfHand;
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

@CardUsed({Extinguish.class, SleightOfHand.class, RagingGoblin.class})
class ExtinguishTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a sorcery spell")
    void castingTargetsSorcerySpell() {
        SleightOfHand sleightOfHand = new SleightOfHand();
        harness.setHand(player1, List.of(sleightOfHand));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setHand(player2, List.of(new Extinguish()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, sleightOfHand.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry extinguishEntry = gd.stack.getLast();
        assertThat(extinguishEntry.getTargetId()).isEqualTo(sleightOfHand.getId());
    }

    @Test
    @DisplayName("Resolving counters the sorcery spell")
    void countersSorcerySpell() {
        SleightOfHand sleightOfHand = new SleightOfHand();
        harness.setHand(player1, List.of(sleightOfHand));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setHand(player2, List.of(new Extinguish()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sleightOfHand.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Sleight of Hand");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-sorcery spell")
    void cannotTargetNonSorcerySpell() {
        RagingGoblin goblin = new RagingGoblin();
        harness.setHand(player1, List.of(goblin));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new Extinguish()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
