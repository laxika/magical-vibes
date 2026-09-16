package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BallLightning;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.FleshToDust;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TreefolkUmbra.class, BallLightning.class, GrizzlyBears.class, FountainOfYouth.class,
        DoomBlade.class, FleshToDust.class})
class TreefolkUmbraTest extends BaseCardTest {

    @Test
    @DisplayName("Treefolk Umbra gives the enchanted creature +0/+2 and toughness-based combat damage")
    void enchantedCreatureGetsBoostAndUsesToughnessForCombatDamage() {
        Permanent ballLightning = addReadyCreature(player1, new BallLightning());
        attachAura(ballLightning);

        assertThat(gqs.getEffectivePower(gd, ballLightning)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ballLightning)).isEqualTo(3);
        assertThat(gqs.getEffectiveCombatDamage(gd, ballLightning)).isEqualTo(3);
    }

    @Test
    @DisplayName("Umbra armor saves an enchanted creature from lethal damage and destroys Treefolk Umbra")
    void umbraArmorSavesFromLethalDamage() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        Permanent aura = attachAura(creature);
        creature.setMarkedDamage(4);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Treefolk Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    @DisplayName("Umbra armor saves an enchanted creature from a destroy effect")
    void umbraArmorSavesFromDestroyEffect() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        attachAura(creature);

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Treefolk Umbra");
    }

    @Test
    @DisplayName("Treefolk Umbra cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new TreefolkUmbra()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = new Permanent(new TreefolkUmbra());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
