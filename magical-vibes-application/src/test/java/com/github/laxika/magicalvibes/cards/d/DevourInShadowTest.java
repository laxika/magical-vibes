package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DevourInShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and its controller loses life equal to its toughness")
    void destroysCreatureAndLosesLifeEqualToToughness() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setToughnessModifier(2);
        castDevourInShadow(target);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Destroys a creature with a regeneration shield")
    void cannotBeRegenerated() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setRegenerationShield(1);
        castDevourInShadow(target);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new DevourInShadow()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mountain.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDevourInShadow(Permanent target) {
        harness.setHand(player1, List.of(new DevourInShadow()));
        addMana();
        harness.castInstant(player1, 0, List.of(target.getId()));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
