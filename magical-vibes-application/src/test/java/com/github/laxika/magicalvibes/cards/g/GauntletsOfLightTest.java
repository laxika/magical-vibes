package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BallLightning;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GauntletsOfLight.class, BallLightning.class, FountainOfYouth.class, GoblinPiker.class})
class GauntletsOfLightTest extends BaseCardTest {

    @Test
    @DisplayName("Gauntlets of Light gives the enchanted creature +0/+2 and toughness-based combat damage")
    void enchantedCreatureGetsBoostAndUsesToughnessForCombatDamage() {
        Permanent ballLightning = addCreatureReady(player1, new BallLightning());
        Permanent aura = new Permanent(new GauntletsOfLight());
        aura.setAttachedTo(ballLightning.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, ballLightning)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ballLightning)).isEqualTo(3);
        assertThat(gqs.getEffectiveCombatDamage(gd, ballLightning)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enchanted creature can pay {2}{W} to untap itself")
    void enchantedCreatureCanUntapItself() {
        Permanent piker = addCreatureReady(player1, new GoblinPiker());
        piker.tap();
        Permanent aura = new Permanent(new GauntletsOfLight());
        aura.setAttachedTo(piker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(piker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Removing Gauntlets of Light removes its granted effects")
    void effectsStopWhenAuraIsRemoved() {
        Permanent piker = addCreatureReady(player1, new GoblinPiker());
        Permanent aura = new Permanent(new GauntletsOfLight());
        aura.setAttachedTo(piker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectiveToughness(gd, piker)).isEqualTo(1);
        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Gauntlets of Light cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GauntletsOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

}
