package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FrenziedGoblin;
import com.github.laxika.magicalvibes.cards.s.ScreechingGriffin;
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

@CardUsed({RollingSpoil.class, Forest.class, FrenziedGoblin.class, ScreechingGriffin.class})
class RollingSpoilTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target land without black mana and leaves creatures unchanged")
    void destroysLandWithoutBlackMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ScreechingGriffin());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new ScreechingGriffin());

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
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ScreechingGriffin());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new ScreechingGriffin());

        castRollingSpoil(target, true);

        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The black mana debuff destroys creatures reduced to zero toughness")
    void blackManaDebuffKillsSmallCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player2, new FrenziedGoblin());

        castRollingSpoil(target, true);

        harness.assertNotOnBattlefield(player2, "Frenzied Goblin");
        harness.assertInGraveyard(player2, "Frenzied Goblin");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScreechingGriffin());
        harness.setHand(player1, List.of(new RollingSpoil()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An illegal land target prevents the black mana debuff from resolving")
    void illegalTargetPreventsAllEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScreechingGriffin());
        harness.setHand(player1, List.of(new RollingSpoil()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        harness.assertInGraveyard(player1, "Rolling Spoil");
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Black mana added after casting does not enable the debuff")
    void blackManaMustBeSpentToCast() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScreechingGriffin());
        harness.setHand(player1, List.of(new RollingSpoil()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest");
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can destroy your own land and affects only creatures present at resolution")
    void ownLandAndCreaturesEnteringLater() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScreechingGriffin());

        castRollingSpoil(target, true);
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new ScreechingGriffin());

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);
        assertThat(laterCreature.getEffectivePower()).isEqualTo(2);
        assertThat(laterCreature.getEffectiveToughness()).isEqualTo(2);
    }

    private void castRollingSpoil(Permanent target, boolean blackManaSpent) {
        harness.setHand(player1, List.of(new RollingSpoil()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, blackManaSpent ? ManaColor.BLACK : ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());
    }
}
