package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.cards.e.EnchantmentAlteration;
import com.github.laxika.magicalvibes.cards.m.MazeOfIth;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TangleKelp.class, MazeOfIth.class, Squire.class, Confiscate.class, EnchantmentAlteration.class})
class TangleKelpTest extends BaseCardTest {

    @Test
    @DisplayName("Tangle Kelp taps the enchanted creature when it enters")
    void tapsEnchantedCreatureOnEnter() {
        Permanent creature = addCreatureReady(player2, new Squire());

        harness.setHand(player1, List.of(new TangleKelp()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tangle Kelp prevents a creature that attacked last turn from untapping")
    void preventsUntapAfterCreatureAttackedLastTurn() {
        Permanent creature = addCreatureReady(player2, new Squire());
        creature.tap();
        creature.setAttackedDuringControllersCurrentTurn(true);
        attachTangleKelp(creature);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tangle Kelp does not prevent untapping when the creature did not attack last turn")
    void allowsUntapAfterCreatureDidNotAttackLastTurn() {
        Permanent creature = addCreatureReady(player2, new Squire());
        creature.tap();
        attachTangleKelp(creature);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tangle Kelp's restriction expires after a controller turn without attacking")
    void allowsUntapAfterAnIdleTurn() {
        Permanent creature = addCreatureReady(player2, new Squire());
        creature.tap();
        creature.setAttackedDuringControllersCurrentTurn(true);
        attachTangleKelp(creature);

        advanceToNextTurn(player1);
        assertThat(creature.isTapped()).isTrue();

        advanceToNextTurn(player2);
        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tangle Kelp cannot enchant a land")
    void cannotEnchantLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new MazeOfIth());

        harness.setHand(player1, List.of(new TangleKelp()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The entry trigger taps the creature currently enchanted after the Aura moves")
    void entryTriggerFollowsMovedAura() {
        Permanent original = addCreatureReady(player2, new Squire());
        Permanent destination = addCreatureReady(player2, new Squire());
        harness.setHand(player1, List.of(new TangleKelp(), new EnchantmentAlteration()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, original.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Tangle Kelp");
        assertThat(original.isTapped()).isFalse();
        harness.castAndResolveInstant(player1, 0, aura.getId());
        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
        resolveAllTriggers();

        assertThat(original.isTapped()).isFalse();
        assertThat(destination.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An attack during a former controller's turn does not restrict the new controller's untap")
    void formerControllersAttackDoesNotPreventUntap() {
        Permanent creature = addCreatureReady(player2, new Squire());
        attachTangleKelp(creature);
        declareAttackers(player2, List.of(0));
        advanceToNextTurn(player2);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new Confiscate()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.isTapped()).isTrue();

        advanceToNextTurn(player1);
        advanceToNextTurn(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Maze of Ith can untap an enchanted attacking creature")
    void allowsUntappingWithAnAbility() {
        Permanent creature = addCreatureReady(player2, new Squire());
        harness.addToBattlefield(player1, new MazeOfIth());
        attachTangleKelp(creature);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));
        assertThat(creature.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
        advanceToNextTurn(player2);
        creature.tap();
        advanceToNextTurn(player1);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing the Aura restores normal untapping even after an attack")
    void removingAuraRestoresUntap() {
        Permanent creature = addCreatureReady(player2, new Squire());
        creature.tap();
        creature.setAttackedDuringControllersCurrentTurn(true);
        attachTangleKelp(creature);
        Permanent aura = findPermanent(player1, "Tangle Kelp");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tangle Kelp can enchant and tap its controller's own creature")
    void tapsOwnCreatureOnEnter() {
        Permanent creature = addCreatureReady(player1, new Squire());
        harness.setHand(player1, List.of(new TangleKelp()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Tangle Kelp").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the enchanted creature that actually attacked stays tapped")
    void distinguishesAttackingAndNonattackingCreatures() {
        Permanent attacker = addCreatureReady(player2, new Squire());
        Permanent idleCreature = addCreatureReady(player2, new Squire());
        attachTangleKelp(attacker);
        attachTangleKelp(idleCreature);

        declareAttackers(player2, List.of(0));
        idleCreature.tap();
        advanceToNextTurn(player2);
        advanceToNextTurn(player1);

        assertThat(attacker.isTapped()).isTrue();
        assertThat(idleCreature.isTapped()).isFalse();
    }

    private void attachTangleKelp(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TangleKelp());
        aura.setAttachedTo(creature.getId());
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Player nextActivePlayer = currentActivePlayer.getId().equals(player1.getId()) ? player2 : player1;
        harness.passUntil(nextActivePlayer, TurnStep.UNTAP);
    }
}
