package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrosissCharm.class, Forest.class, GrizzlyBears.class, MassOfGhouls.class, Ornithopter.class})
class CrosissCharmTest extends BaseCardTest {

    @Test
    void returnsTargetPermanentToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void returnsTargetLandToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        cast(0, target.getId());

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    void destroysTargetNonblackCreatureWithoutRegeneration() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setRegenerationShield(1);
        cast(1, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetBlackCreatureWithNonblackCreatureMode() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MassOfGhouls());
        setUpSpell();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroysTargetArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        cast(2, target.getId());

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    void cannotTargetNonArtifactPermanentWithArtifactMode() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        setUpSpell();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, java.util.UUID targetId) {
        setUpSpell();
        harness.castModalInstant(player1, 0, mode, List.of(targetId));
        harness.passBothPriorities();
    }

    private void setUpSpell() {
        harness.setHand(player1, List.of(new CrosissCharm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
