package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GaeasEmbrace.class, GorillaWarrior.class, WornPowerstone.class})
class GaeasEmbraceTest extends BaseCardTest {

    @Test
    void enchantedCreatureGetsBoostAndTrample() {
        Permanent creature = addReadyCreature();
        attachAura(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void enchantedCreatureCanRegenerate() {
        Permanent creature = addReadyCreature();
        Permanent aura = attachAura(creature);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void auraControllerCanRegenerateOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new GorillaWarrior());
        Permanent aura = attachAura(creature);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void effectsEndWhenAuraLeavesBattlefield() {
        Permanent creature = addReadyCreature();
        Permanent aura = attachAura(creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        harness.addToBattlefield(player1, new WornPowerstone());
        harness.setHand(player1, List.of(new GaeasEmbrace()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        Permanent artifact = findPermanent(player1, "Worn Powerstone");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyCreature() {
        return addCreatureReady(player1, new GorillaWarrior());
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GaeasEmbrace());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
