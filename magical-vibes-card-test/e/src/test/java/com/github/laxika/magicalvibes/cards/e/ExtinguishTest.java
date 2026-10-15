package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AmbitionsCost;
import com.github.laxika.magicalvibes.cards.f.ForestBear;
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

@CardUsed({Extinguish.class, AmbitionsCost.class, ForestBear.class})
class ExtinguishTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a sorcery spell")
    void castingTargetsSorcerySpell() {
        AmbitionsCost ambitionsCost = new AmbitionsCost();
        harness.castFromHand(player1, ambitionsCost, "{3}{B}");

        harness.setHand(player2, List.of(new Extinguish()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, ambitionsCost.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry extinguishEntry = gd.stack.getLast();
        assertThat(extinguishEntry.getTargetId()).isEqualTo(ambitionsCost.getId());
    }

    @Test
    @DisplayName("Resolving counters the sorcery spell")
    void countersSorcerySpell() {
        AmbitionsCost ambitionsCost = new AmbitionsCost();
        harness.castFromHand(player1, ambitionsCost, "{3}{B}");

        harness.setHand(player2, List.of(new Extinguish()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, ambitionsCost.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Ambition's Cost");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-sorcery spell")
    void cannotTargetNonSorcerySpell() {
        ForestBear forestBear = new ForestBear();
        harness.castFromHand(player1, forestBear, "{1}{G}");

        harness.setHand(player2, List.of(new Extinguish()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, forestBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can counter a sorcery controlled by its own controller")
    void countersOwnSorcery() {
        AmbitionsCost ambitionsCost = new AmbitionsCost();
        harness.castFromHand(player1, ambitionsCost, "{3}{B}");

        harness.setHand(player1, List.of(new Extinguish()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, ambitionsCost.getId());

        harness.assertInGraveyard(player1, "Ambition's Cost");
        harness.assertInGraveyard(player1, "Extinguish");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an instant even when a sorcery is also on the stack")
    void cannotTargetInstantSpell() {
        AmbitionsCost ambitionsCost = new AmbitionsCost();
        harness.castFromHand(player1, ambitionsCost, "{3}{B}");

        Extinguish firstExtinguish = new Extinguish();
        harness.setHand(player2, List.of(firstExtinguish));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, ambitionsCost.getId());

        harness.setHand(player1, List.of(new Extinguish()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, firstExtinguish.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(2);
        harness.assertInHand(player1, "Extinguish");
    }

}
