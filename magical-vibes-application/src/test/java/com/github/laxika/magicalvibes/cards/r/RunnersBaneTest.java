package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RunnersBaneTest extends BaseCardTest {

    @Test
    @DisplayName("Runner's Bane taps the enchanted creature on enter")
    void tapsEnchantedCreatureOnEnter() {
        Permanent creature = addCreatureReady(player2, new HillGiant());
        assertThat(creature.isTapped()).isFalse();

        harness.setHand(player1, List.of(new RunnersBane()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve the Aura
        harness.passBothPriorities(); // resolve the ETB tap trigger

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Runner's Bane")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new HillGiant());
        creature.tap();

        Permanent aura = new Permanent(new RunnersBane());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature untaps again once Runner's Bane leaves the battlefield")
    void untapsAfterAuraRemoved() {
        Permanent creature = addCreatureReady(player2, new HillGiant());
        creature.tap();

        Permanent aura = new Permanent(new RunnersBane());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Runner's Bane cannot enchant a creature with power 4")
    void cannotEnchantHighPowerCreature() {
        Permanent bigCreature = addCreatureReady(player2, new AirElemental());

        harness.setHand(player1, List.of(new RunnersBane()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bigCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Runner's Bane"));
    }

    @Test
    @DisplayName("Runner's Bane fizzles if the target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new RunnersBane()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runner's Bane");
        harness.assertNotOnBattlefield(player1, "Runner's Bane");
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
