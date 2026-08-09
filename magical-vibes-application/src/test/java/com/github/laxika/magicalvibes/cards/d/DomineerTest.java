package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomineerTest extends BaseCardTest {

    @Test
    void resolvingDomineerStealsArtifactCreature() {
        Permanent artifactCreature = addCreatureReady(player2, new Ornithopter());

        harness.setHand(player1, List.of(new Domineer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, artifactCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(artifactCreature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(artifactCreature.getId()));
    }

    @Test
    void cannotEnchantNonArtifactCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Domineer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotEnchantNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2,
                new FountainOfYouth());

        harness.setHand(player1, List.of(new Domineer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
