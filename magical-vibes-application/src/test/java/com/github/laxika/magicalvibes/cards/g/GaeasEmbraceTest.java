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

    @Test
    void castingAuraOnOpponentCreatureAppliesOnlyToThatCreature() {
        Permanent ownCreature = addReadyCreature();
        Permanent enchanted = addCreatureReady(player2, new GorillaWarrior());
        harness.setHand(player1, List.of(new GaeasEmbrace()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Gaea's Embrace").getAttachedTo()).isEqualTo(enchanted.getId());
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void regenerationPreventsDestructionAndTapsCreature() {
        Permanent creature = addReadyCreature();
        Permanent aura = attachAura(creature);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        harness.inMutationScope(() -> assertThat(
                harness.getPermanentRemovalService().tryDestroyPermanent(gd, creature)).isFalse());

        harness.assertOnBattlefield(player1, "Gorilla Warrior");
        harness.assertOnBattlefield(player1, "Gaea's Embrace");
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getRegenerationShield()).isZero();
    }

    @Test
    void regenerationResolvesAfterAuraIsDestroyedInResponse() {
        Permanent creature = addReadyCreature();
        Permanent aura = attachAura(creature);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, aura));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gaea's Embrace");
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void creatureControllerCannotActivateOpponentAura() {
        Permanent creature = addCreatureReady(player2, new GorillaWarrior());
        attachAura(creature);
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        assertThat(creature.getRegenerationShield()).isZero();
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
