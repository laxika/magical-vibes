package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({RollingSpoil.class, Forest.class, FugitiveWizard.class, GrizzlyBears.class})
class RollingSpoilTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target land without black mana and leaves creatures unchanged")
    void destroysLandWithoutBlackMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castRollingSpoil(target, false);

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Black mana gives all creatures -1/-1 until end of turn")
    void blackManaAppliesCreatureDebuff() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castRollingSpoil(target, true);

        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The black mana debuff destroys creatures reduced to zero toughness")
    void blackManaDebuffKillsSmallCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new FugitiveWizard());

        castRollingSpoil(target, true);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Fugitive Wizard"));
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RollingSpoil()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRollingSpoil(Permanent target, boolean blackManaSpent) {
        harness.setHand(player1, List.of(new RollingSpoil()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, blackManaSpent ? ManaColor.BLACK : ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
