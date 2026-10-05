package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PilloryOfTheSleepless.class, Gristleback.class, GruulSignet.class})
class PilloryOfTheSleeplessTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1, new Gristleback());
        attachAura(player2, creature);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new Gristleback());
        attachAura(player1, blocker);

        Permanent attacker = addCreatureReady(player1, new Gristleback());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Controller of enchanted creature loses 1 life at upkeep")
    void enchantedCreatureControllerLosesLifeAtUpkeep() {
        Permanent creature = addCreatureReady(player2, new Gristleback());
        attachAura(player1, creature);
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life - 1);
    }

    @Test
    @DisplayName("Resolving Pillory attaches it to the target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player2, new Gristleback());
        PilloryOfTheSleepless pillory = new PilloryOfTheSleepless();
        harness.setHand(player1, List.of(pillory));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == pillory
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GruulSignet());
        harness.setHand(player1, List.of(new PilloryOfTheSleepless()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The Aura controller's upkeep does not trigger the enchanted opponent's ability")
    void doesNotTriggerOnAuraControllersUpkeep() {
        Permanent creature = addCreatureReady(player2, new Gristleback());
        attachAura(player1, creature);
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life);
    }

    @Test
    @DisplayName("Two Pillories grant two independent upkeep abilities")
    void multiplePilloriesEachCauseLifeLoss() {
        Permanent creature = addCreatureReady(player2, new Gristleback());
        attachAura(player1, creature);
        attachAura(player1, creature);
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Removing Pillory after the upkeep trigger does not stop life loss")
    void triggerResolvesAfterAuraLeavesBattlefield() {
        Permanent creature = addCreatureReady(player2, new Gristleback());
        Permanent aura = attachAura(player1, creature);
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Removing Pillory before upkeep removes the granted ability and combat restrictions")
    void removingAuraEndsItsEffects() {
        Permanent creature = addCreatureReady(player2, new Gristleback());
        Permanent aura = attachAura(player1, creature);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThat(creature.isAttacking()).isTrue();
    }

    private Permanent attachAura(Player auraController, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new PilloryOfTheSleepless());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
