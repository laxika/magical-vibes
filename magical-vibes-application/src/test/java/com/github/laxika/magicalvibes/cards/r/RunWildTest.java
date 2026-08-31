package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RunWild.class, GrizzlyBears.class, Shock.class, Forest.class})
class RunWildTest extends BaseCardTest {

    @Test
    @DisplayName("Grants target creature trample and a regeneration ability")
    void grantsTrampleAndRegenerationAbility() {
        Permanent creature = castRunWildOnOwnCreature();

        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The granted regeneration shield saves the creature from lethal damage")
    void regenerationSavesCreatureFromLethalDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RunWild(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.getRegenerationShield()).isZero();
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Trample and the granted ability wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = castRunWildOnOwnCreature();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new RunWild()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent castRunWildOnOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RunWild()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        return creature;
    }
}
