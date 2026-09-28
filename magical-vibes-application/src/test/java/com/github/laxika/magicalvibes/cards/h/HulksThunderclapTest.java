package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GammaGrotesque;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HulksThunderclap.class, GammaGrotesque.class, GrizzlyBears.class, HowlingMine.class})
class HulksThunderclapTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage from a creature you control without beholding")
    void dealsPowerDamageWithoutBeholding() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HulksThunderclap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(
                source.getId(), harness.getPermanentId(player2, "Grizzly Bears")));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys a noncreature artifact when a Gamma is beheld")
    void destroysArtifactWhenGammaIsBeheld() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent gamma = harness.addToBattlefieldAndReturn(player1, new GammaGrotesque());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new HowlingMine());
        harness.setHand(player1, List.of(new HulksThunderclap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithBehold(player1, 0, null,
                List.of(source.getId(), harness.getPermanentId(player2, "Grizzly Bears"), artifact.getId()),
                List.of(gamma.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Howling Mine");
    }

    @Test
    @DisplayName("The optional target must be a noncreature artifact or enchantment")
    void rejectsCreatureAsBonusTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent gamma = harness.addToBattlefieldAndReturn(player1, new GammaGrotesque());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HulksThunderclap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithBehold(player1, 0, null,
                List.of(source.getId(), victim.getId(), gamma.getId()), List.of(gamma.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature artifact or noncreature enchantment");
    }
}
