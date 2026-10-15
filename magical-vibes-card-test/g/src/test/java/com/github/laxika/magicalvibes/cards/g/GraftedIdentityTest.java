package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DawnhartRejuvenator;
import com.github.laxika.magicalvibes.cards.m.MoonsilverKey;
import com.github.laxika.magicalvibes.cards.r.ReturnToNature;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraftedIdentity.class, DawnhartRejuvenator.class, MoonsilverKey.class, ReturnToNature.class})
class GraftedIdentityTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Grafted Identity sacrifices a creature and steals the enchanted creature")
    void sacrificesCreatureAndStealsEnchantedCreature() {
        Permanent sacrifice = addCreatureReady(player1, new DawnhartRejuvenator());
        Permanent creature = addCreatureReady(player2, new DawnhartRejuvenator());

        harness.setHand(player1, List.of(new GraftedIdentity()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, creature.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dawnhart Rejuvenator");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Grafted Identity cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent sacrifice = addCreatureReady(player1, new DawnhartRejuvenator());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MoonsilverKey());

        harness.setHand(player1, List.of(new GraftedIdentity()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, artifact.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Grafted Identity requires sacrificing a creature even with enough mana")
    void cannotCastWithoutSacrifice() {
        Permanent creature = addCreatureReady(player2, new DawnhartRejuvenator());
        harness.setHand(player1, List.of(new GraftedIdentity()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Grafted Identity");
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new DawnhartRejuvenator());
        harness.setHand(player1, List.of(new GraftedIdentity()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, creature.getId(), creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Dawnhart Rejuvenator");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The targeted creature can be sacrificed, causing the Aura to fail to resolve")
    void canSacrificeTargetedCreature() {
        Permanent creature = addCreatureReady(player1, new DawnhartRejuvenator());
        harness.setHand(player1, List.of(new GraftedIdentity()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, creature.getId(), creature.getId());

        harness.assertInGraveyard(player1, "Dawnhart Rejuvenator");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grafted Identity");
        harness.assertNotOnBattlefield(player1, "Grafted Identity");
    }

    @Test
    @DisplayName("Grafted Identity can enchant a creature already controlled by its caster")
    void canEnchantOwnCreature() {
        Permanent sacrifice = addCreatureReady(player1, new DawnhartRejuvenator());
        Permanent creature = addCreatureReady(player1, new DawnhartRejuvenator());
        harness.setHand(player1, List.of(new GraftedIdentity()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, creature.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grafted Identity").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
        harness.assertInGraveyard(player1, "Dawnhart Rejuvenator");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Destroying Grafted Identity restores control and removes its bonus")
    void destroyingAuraRestoresControlAndRemovesBoost() {
        Permanent sacrifice = addCreatureReady(player1, new DawnhartRejuvenator());
        Permanent creature = addCreatureReady(player2, new DawnhartRejuvenator());
        harness.setHand(player1, List.of(new GraftedIdentity()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorceryWithSacrifice(player1, 0, creature.getId(), sacrifice.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Grafted Identity");
        harness.assertOnBattlefield(player1, "Dawnhart Rejuvenator");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        harness.setHand(player2, List.of(new ReturnToNature()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, 1, aura.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grafted Identity");
        harness.assertOnBattlefield(player2, "Dawnhart Rejuvenator");
        harness.assertNotOnBattlefield(player1, "Dawnhart Rejuvenator");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }
}
