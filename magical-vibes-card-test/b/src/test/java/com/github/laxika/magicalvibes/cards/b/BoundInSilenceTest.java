package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CoalitionRelic;
import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoundInSilence.class, FomoriNomad.class, CoalitionRelic.class})
class BoundInSilenceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Bound in Silence attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = addCreatureReady(player2);

        castAuraOn(bears);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof BoundInSilence
                        && permanent.isAttached()
                        && bears.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature cannot be declared as an attacker")
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1);
        attachAura(player2, creature);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot be declared as a blocker")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2);
        attachAura(player1, blocker);
        Permanent attacker = addCreatureReady(player1);
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Removing Bound in Silence restores the creature's combat abilities")
    void removingAuraRestoresCombatAbilities() {
        Permanent creature = addCreatureReady(player1);
        Permanent aura = attachAura(player2, creature);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        gd.playerBattlefields.get(player2.getId()).remove(aura);

        declareAttackers(player1, List.of(0));
    }

    @Test
    @DisplayName("Bound in Silence fizzles if its target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = addCreatureReady(player2);

        harness.setHand(player1, List.of(new BoundInSilence()));
        addMana();
        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Bound in Silence cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent relic = harness.addToBattlefieldAndReturn(player2, new CoalitionRelic());

        harness.setHand(player1, List.of(new BoundInSilence()));
        addMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, relic.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Bound in Silence can enchant its controller's creature")
    void canEnchantOwnCreature() {
        Permanent creature = addCreatureReady(player1);

        castAuraOn(creature);

        assertThat(findPermanent(player1, "Bound in Silence").getAttachedTo())
                .isEqualTo(creature.getId());
        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Bound in Silence does not restrict other creatures")
    void otherCreatureCanAttack() {
        Permanent enchanted = addCreatureReady(player1);
        addCreatureReady(player1);
        attachAura(player2, enchanted);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player1, List.of(1));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("An already attacking creature still deals damage after becoming enchanted")
    void alreadyAttackingCreatureStillDealsCombatDamage() {
        Permanent attacker = addCreatureReady(player1);
        attacker.setAttacking(true);
        attachAura(player2, attacker);
        harness.forceActivePlayer(player1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    private void castAuraOn(Permanent target) {
        harness.setHand(player1, List.of(new BoundInSilence()));
        addMana();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent attachAura(Player controller, Permanent target) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new BoundInSilence());
        aura.setAttachedTo(target.getId());
        return aura;
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new FomoriNomad());
    }
}
