package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
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

@CardUsed({TemporalIsolation.class, FountainOfYouth.class, GrizzlyBears.class, LightningBolt.class, ZuranSpellcaster.class})
class TemporalIsolationTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has shadow")
    void enchantedCreatureHasShadow() {
        Permanent creature = addCreature(player1);

        enchant(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("A non-shadow creature cannot block the enchanted creature")
    void nonShadowCreatureCannotBlockEnchantedAttacker() {
        Permanent attacker = addCreature(player1);
        attacker.setAttacking(true);
        enchant(attacker);
        addCreature(player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new com.github.laxika.magicalvibes.networking.message.BlockerAssignment(0, 0)
        ))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature deals no combat damage")
    void enchantedCreatureDealsNoCombatDamage() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreature(player1);
        attacker.setAttacking(true);
        enchant(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Enchanted creature deals no noncombat damage")
    void enchantedCreatureDealsNoNoncombatDamage() {
        harness.setLife(player2, 20);
        Permanent spellcaster = new Permanent(new ZuranSpellcaster());
        spellcaster.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(spellcaster);
        enchant(spellcaster);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Damage dealt to the enchanted creature is not prevented")
    void damageToEnchantedCreatureStillApplies() {
        Permanent creature = addCreature(player2);
        enchant(creature);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        harness.setHand(player1, List.of(new TemporalIsolation()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent enchant(Permanent creature) {
        Permanent aura = new Permanent(new TemporalIsolation());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
