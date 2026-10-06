package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.ScaldingDevil;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuiseOfFire.class, GrizzlyBears.class, FountainOfYouth.class, ScaldingDevil.class})
class GuiseOfFireTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Guise of Fire attaches it and gives the creature +1/-1")
    void resolvingAttachesAndBoosts() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GuiseOfFire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Guise of Fire")
                        && bears.getId().equals(p.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enchanted creature must attack each combat if able")
    void enchantedCreatureMustAttackWhenAble() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GuiseOfFire());
        aura.setAttachedTo(bears.getId());

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Guise of Fire")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GuiseOfFire()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void tappedEnchantedCreatureDoesNotHaveToAttack() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GuiseOfFire());
        aura.setAttachedTo(bears.getId());

        declareAttackers(player1, List.of());

        assertThat(bears.isAttacking()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    void summoningSickEnchantedCreatureDoesNotHaveToAttack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(true);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GuiseOfFire());
        aura.setAttachedTo(bears.getId());

        declareAttackers(player1, List.of());

        assertThat(bears.isAttacking()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    void opponentCreatureGetsBoostAndMustAttackItsControllersCombat() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GuiseOfFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
        assertThat(findPermanent(player1, "Guise of Fire").getAttachedTo()).isEqualTo(bears.getId());
        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void toughnessReductionKillsCreatureAndPutsAuraInOwnersGraveyard() {
        Permanent devil = harness.addToBattlefieldAndReturn(player2, new ScaldingDevil());
        harness.setHand(player1, List.of(new GuiseOfFire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, devil.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Scalding Devil");
        harness.assertInGraveyard(player2, "Scalding Devil");
        harness.assertNotOnBattlefield(player1, "Guise of Fire");
        harness.assertInGraveyard(player1, "Guise of Fire");
    }
}
