package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DrifterIlDal;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBaloth;
import com.github.laxika.magicalvibes.cards.f.FledglingMawcor;
import com.github.laxika.magicalvibes.cards.h.Hivestone;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemporalIsolation.class, DurkwoodBaloth.class, DrifterIlDal.class,
        FledglingMawcor.class, Hivestone.class})
class TemporalIsolationTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has shadow")
    void enchantedCreatureHasShadow() {
        Permanent creature = addCreatureReady(player1, new DurkwoodBaloth());

        enchant(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("A non-shadow creature cannot block the enchanted creature")
    void nonShadowCreatureCannotBlockEnchantedAttacker() {
        Permanent attacker = addCreatureReady(player1, new DurkwoodBaloth());
        attacker.setAttacking(true);
        enchant(attacker);
        addCreatureReady(player2, new DurkwoodBaloth());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0)
        ))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature deals no combat damage to a shadow blocker")
    void enchantedCreatureDealsNoCombatDamageToShadowBlocker() {
        Permanent attacker = addCreatureReady(player1, new DurkwoodBaloth());
        attacker.setAttacking(true);
        enchant(attacker);
        Permanent blocker = addCreatureReady(player2, new DrifterIlDal());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Drifter il-Dal");
    }

    @Test
    @DisplayName("Enchanted creature deals no combat damage")
    void enchantedCreatureDealsNoCombatDamage() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new DurkwoodBaloth());
        attacker.setAttacking(true);
        enchant(attacker);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Enchanted creature deals no noncombat damage")
    void enchantedCreatureDealsNoNoncombatDamage() {
        harness.setLife(player2, 20);
        Permanent spellcaster = addCreatureReady(player1, new FledglingMawcor());
        enchant(spellcaster);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Damage dealt to the enchanted creature is not prevented")
    void damageToEnchantedCreatureStillApplies() {
        Permanent creature = addCreatureReady(player2, new DrifterIlDal());
        enchant(creature);
        Permanent spellcaster = addCreatureReady(player1, new FledglingMawcor());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(spellcaster),
                null, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Drifter il-Dal");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Hivestone());
        harness.setHand(player1, List.of(new TemporalIsolation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can be cast during an opponent's turn because it has flash")
    void canBeCastDuringOpponentsTurnWithFlash() {
        Permanent creature = addCreatureReady(player2, new DurkwoodBaloth());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TemporalIsolation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.getGameService().passPriority(gd, player2);
        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent enchant(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TemporalIsolation());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
