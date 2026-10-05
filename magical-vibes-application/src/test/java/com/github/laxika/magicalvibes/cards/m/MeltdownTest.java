package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChimericStaff;
import com.github.laxika.magicalvibes.cards.c.ClawsOfGix;
import com.github.laxika.magicalvibes.cards.f.Fluctuator;
import com.github.laxika.magicalvibes.cards.g.GoblinLackey;
import com.github.laxika.magicalvibes.cards.g.GraftedSkullcap;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Meltdown.class, ClawsOfGix.class, Fluctuator.class, GraftedSkullcap.class, GoblinLackey.class, ChimericStaff.class})
class MeltdownTest extends BaseCardTest {

    private void castMeltdown(int xValue) {
        harness.setHand(player1, List.of(new Meltdown()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }

    @Test
    @DisplayName("Destroys artifacts with mana value X or less and spares larger artifacts and nonartifacts")
    void destroysArtifactsWithinManaValueBound() {
        harness.addToBattlefield(player1, new ClawsOfGix());
        harness.addToBattlefield(player2, new Fluctuator());
        harness.addToBattlefield(player2, new GraftedSkullcap());
        harness.addToBattlefield(player2, new GoblinLackey());

        castMeltdown(2);

        harness.assertInGraveyard(player1, "Claws of Gix");
        harness.assertInGraveyard(player2, "Fluctuator");
        harness.assertOnBattlefield(player2, "Grafted Skullcap");
        harness.assertOnBattlefield(player2, "Goblin Lackey");
    }

    @Test
    @DisplayName("X=0 destroys only mana value 0 artifacts")
    void xZeroDestroysOnlyZeroManaValueArtifacts() {
        harness.addToBattlefield(player2, new ClawsOfGix());
        harness.addToBattlefield(player2, new Fluctuator());

        castMeltdown(0);

        harness.assertInGraveyard(player2, "Claws of Gix");
        harness.assertOnBattlefield(player2, "Fluctuator");
    }

    @Test
    @DisplayName("Animated artifacts are destroyed based on mana value, not their power")
    void destroysAnimatedArtifactAtManaValueBoundary() {
        harness.addToBattlefield(player2, new ChimericStaff());
        harness.addMana(player2, ManaColor.COLORLESS, 7);
        harness.activateAbility(player2, 0, 7, null);
        harness.passBothPriorities();

        castMeltdown(4);

        harness.assertInGraveyard(player2, "Chimeric Staff");
        harness.assertNotOnBattlefield(player2, "Chimeric Staff");
    }

    @Test
    @DisplayName("A small animated artifact survives when its mana value exceeds X")
    void sparesAnimatedArtifactAboveManaValueBoundary() {
        harness.addToBattlefield(player2, new ChimericStaff());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, 1, null);
        harness.passBothPriorities();

        castMeltdown(3);

        harness.assertOnBattlefield(player2, "Chimeric Staff");
        harness.assertNotInGraveyard(player2, "Chimeric Staff");
    }

    @Test
    @DisplayName("Resolves without artifacts and leaves nonartifact creatures alone")
    void resolvesWithoutArtifacts() {
        harness.addToBattlefield(player2, new GoblinLackey());

        castMeltdown(4);

        harness.assertInGraveyard(player1, "Meltdown");
        harness.assertOnBattlefield(player2, "Goblin Lackey");
    }
}
