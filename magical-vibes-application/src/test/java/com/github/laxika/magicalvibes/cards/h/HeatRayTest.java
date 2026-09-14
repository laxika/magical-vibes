package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinSpelunkers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeatRay.class, GoblinSpelunkers.class, Forest.class})
class HeatRayTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to the targeted creature, killing it when X is lethal")
    void killsCreatureWithLethalX() {
        Permanent goblins = harness.addToBattlefieldAndReturn(player2, new GoblinSpelunkers());
        harness.setHand(player1, List.of(new HeatRay()));
        harness.addMana(player1, ManaColor.RED, 3); // X=2: {2}{R}

        harness.castInstantForX(player1, 0, 2, List.of(goblins.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Goblin Spelunkers");
    }

    @Test
    @DisplayName("Damage less than toughness leaves the creature alive with damage marked")
    void marksDamageWhenNotLethal() {
        Permanent goblins = harness.addToBattlefieldAndReturn(player2, new GoblinSpelunkers());
        harness.setHand(player1, List.of(new HeatRay()));
        harness.addMana(player1, ManaColor.RED, 2); // X=1

        harness.castInstantForX(player1, 0, 1, List.of(goblins.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Goblin Spelunkers");
        assertThat(goblins.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target a creature while dealing zero damage when X is zero")
    void dealsZeroDamageWhenXIsZero() {
        Permanent goblins = harness.addToBattlefieldAndReturn(player2, new GoblinSpelunkers());
        harness.setHand(player1, List.of(new HeatRay()));
        harness.addMana(player1, ManaColor.RED, 1); // X=0: {R}

        harness.castInstantForX(player1, 0, 0, List.of(goblins.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Goblin Spelunkers");
        assertThat(goblins.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new HeatRay()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID forestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(forestId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
