package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.SchoolOfTheUnseen;
import com.github.laxika.magicalvibes.cards.s.SwornDefender;
import com.github.laxika.magicalvibes.cards.p.PalaceGuard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiftOfTheWoods.class, SwornDefender.class, SchoolOfTheUnseen.class,
        PalaceGuard.class, Naturalize.class, DoomBlade.class})
class GiftOfTheWoodsTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature blocking gets +0/+3 and the aura's controller gains 1 life")
    void blockTriggerBoostsAndGainsLife() {
        harness.setLife(player2, 20);

        Permanent blocker = addReadyCreature(player2);
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        attachGift(player2, blocker);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(21);
    }

    @Test
    void blockingMultipleCreaturesTriggersOnlyOnce() {
        harness.setLife(player2, 20);

        Permanent blocker = addCreatureReady(player2, new PalaceGuard());
        Permanent attacker1 = addReadyCreature(player1);
        Permanent attacker2 = addReadyCreature(player1);
        attacker1.setAttacking(true);
        attacker2.setAttacking(true);
        attachGift(player2, blocker);

        declareBlockers(List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Enchanted attacker becoming blocked gets +0/+3 and the aura's controller gains 1 life")
    void becomesBlockedTriggerBoostsAndGainsLife() {
        harness.setLife(player1, 20);

        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        addReadyCreature(player2);
        attachGift(player1, attacker);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getToughnessModifier()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Becoming blocked by two creatures triggers only once")
    void becomesBlockedTriggersOncePerCombatEvent() {
        harness.setLife(player1, 20);

        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        addReadyCreature(player2);
        addReadyCreature(player2);
        attachGift(player1, attacker);

        declareBlockers(List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getToughnessModifier()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("The aura's controller gains the life even when enchanting an opponent's creature")
    void auraControllerGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent blocker = addReadyCreature(player2);
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        attachGift(player1, blocker);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent blocker = addReadyCreature(player2);
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        attachGift(player2, blocker);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        assertThat(blocker.getToughnessModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("No trigger when a creature that is not enchanted blocks")
    void noTriggerForUnenchantedCreature() {
        Permanent enchanted = addReadyCreature(player2);
        addReadyCreature(player2);
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        attachGift(player2, enchanted);

        declareBlockers(List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new SwornDefender());
        harness.addToBattlefield(player1, new SchoolOfTheUnseen());
        harness.setHand(player1, List.of(new GiftOfTheWoods()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        Permanent land = gd.playerBattlefields.get(player1.getId()).get(0);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canCastOnOpponentsCreatureAndTriggerForAuraController() {
        Permanent blocker = addReadyCreature(player2);
        Permanent attacker = addReadyCreature(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GiftOfTheWoods()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, blocker.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Gift of the Woods").getAttachedTo()).isEqualTo(blocker.getId());
        attacker.setAttacking(true);
        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.getToughnessModifier()).isEqualTo(3);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    void pendingTriggerStillBoostsAndGainsLifeAfterAuraIsDestroyed() {
        harness.setLife(player2, 20);
        Permanent blocker = addReadyCreature(player2);
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        Permanent aura = attachGift(player2, blocker);
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        harness.castInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Gift of the Woods");
        assertThat(blocker.getToughnessModifier()).isEqualTo(3);
        harness.assertLife(player2, 21);
    }

    @Test
    void pendingTriggerStillGainsLifeAfterEnchantedCreatureIsDestroyed() {
        harness.setLife(player2, 20);
        Permanent blocker = addReadyCreature(player2);
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        attachGift(player2, blocker);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        declareBlockers(List.of(new BlockerAssignment(0, 0)));
        harness.castInstant(player1, 0, blocker.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Sworn Defender");
        harness.assertInGraveyard(player2, "Gift of the Woods");
        harness.assertLife(player2, 21);
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new SwornDefender());
    }

    private Permanent attachGift(Player controller, Permanent target) {
        Permanent aura = new Permanent(new GiftOfTheWoods());
        aura.setAttachedTo(target.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private void declareBlockers(List<BlockerAssignment> assignments) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, assignments);
    }
}
