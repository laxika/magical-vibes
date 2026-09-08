package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class MeltdownTest extends BaseCardTest {

    private void castMeltdown(int xValue) {
        harness.setHand(player1, List.of(new Meltdown()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Destroys artifacts with mana value X or less and spares larger artifacts and nonartifacts")
    void destroysArtifactsWithinManaValueBound() {
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player2, new HowlingMine());
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castMeltdown(2);

        harness.assertInGraveyard(player1, "Memnite");
        harness.assertInGraveyard(player2, "Howling Mine");
        harness.assertOnBattlefield(player2, "Rod of Ruin");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("X=0 destroys only mana value 0 artifacts")
    void xZeroDestroysOnlyZeroManaValueArtifacts() {
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new HowlingMine());

        castMeltdown(0);

        harness.assertInGraveyard(player2, "Memnite");
        harness.assertOnBattlefield(player2, "Howling Mine");
    }
}
