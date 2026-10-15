package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArtifactBlast.class, Millstone.class, GrizzlyBears.class, Ornithopter.class})
class ArtifactBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an artifact spell")
    void countersArtifactSpell() {
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new ArtifactBlast()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, millstone.getId());

        harness.assertInGraveyard(player1, "Millstone");
        harness.assertNotOnBattlefield(player1, "Millstone");
        harness.assertInGraveyard(player2, "Artifact Blast");
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature spell")
    void cannotTargetNonartifactCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new ArtifactBlast()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters an artifact creature spell")
    void countersArtifactCreatureSpell() {
        Ornithopter ornithopter = new Ornithopter();
        harness.setHand(player1, List.of(ornithopter));
        harness.setHand(player2, List.of(new ArtifactBlast()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, ornithopter.getId());

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player2, "Artifact Blast");
    }

    @Test
    @DisplayName("Cannot target an artifact's activated ability")
    void cannotTargetArtifactActivatedAbility() {
        harness.addToBattlefield(player1, new Millstone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new ArtifactBlast()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        var abilityId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, abilityId))
                .isInstanceOf(IllegalStateException.class);
    }
}
