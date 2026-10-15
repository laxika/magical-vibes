package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.v.VampireNoble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MidnightArsonist.class, MindStone.class, Ornithopter.class, VampireNoble.class})
class MidnightArsonistTest extends BaseCardTest {

    @Test
    void destroysUpToTheNumberOfVampiresYouControl() {
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addToBattlefield(player1, new VampireNoble());

        castArsonist();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, firstArtifact.getId());
        harness.handlePermanentChosen(player1, secondArtifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ornithopter");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent == firstArtifact || permanent == secondArtifact);
    }

    @Test
    void theArsonistCountsItselfAsAVampire() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castArsonist();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertOnBattlefield(player1, "Midnight Arsonist");
    }

    @Test
    void cannotTargetAnArtifactWithAManaAbility() {
        Permanent manaArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        castArsonist();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, manaArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castArsonist() {
        harness.setHand(player1, List.of(new MidnightArsonist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
