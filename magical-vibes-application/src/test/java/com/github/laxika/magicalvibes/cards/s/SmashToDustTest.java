package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SmashToDust.class, FountainOfYouth.class, WallOfWood.class, GrizzlyBears.class})
class SmashToDustTest extends BaseCardTest {

    @Test
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        cast(0, artifact.getId());

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    void destroysTargetCreatureWithDefender() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfWood());

        cast(1, wall.getId());

        harness.assertNotOnBattlefield(player2, "Wall of Wood");
        harness.assertInGraveyard(player2, "Wall of Wood");
    }

    @Test
    void defenderModeCannotTargetCreatureWithoutDefender() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damagesOnlyCreaturesOpponentsControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentWall = harness.addToBattlefieldAndReturn(player2, new WallOfWood());

        cast(2, null);

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(opponentWall.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void artifactModeCannotTargetNonArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int modeIndex, java.util.UUID targetId) {
        prepareSpell();
        harness.castSorcery(player1, 0, modeIndex, targetId);
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new SmashToDust()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
