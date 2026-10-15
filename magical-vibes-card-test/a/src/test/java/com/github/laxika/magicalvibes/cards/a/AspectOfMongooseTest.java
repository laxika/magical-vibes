package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KrosanGrip;
import com.github.laxika.magicalvibes.cards.w.WipeAway;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AspectOfMongoose.class, AshcoatBear.class, Forest.class, WipeAway.class, KrosanGrip.class})
class AspectOfMongooseTest extends BaseCardTest {

    @Test
    @DisplayName("Aspect of Mongoose gives the enchanted creature shroud")
    void enchantedCreatureHasShroud() {
        Permanent creature = addCreature();
        attachAura(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Aspect of Mongoose can be cast attached to a creature")
    void canBeCastAttachedToCreature() {
        Permanent creature = addCreature();
        harness.setHand(player1, List.of(new AspectOfMongoose()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof AspectOfMongoose
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Aspect of Mongoose cannot enchant a noncreature permanent")
    void cannotEnchantNonCreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new AspectOfMongoose()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Shroud prevents targeting the enchanted creature")
    void shroudPreventsTargetingEnchantedCreature() {
        Permanent creature = addCreature();
        attachAura(creature);
        harness.setHand(player1, List.of(new WipeAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Aspect of Mongoose returns to its owner's hand when put into a graveyard from the battlefield")
    void returnsToHandWhenDestroyed() {
        Permanent creature = addCreature();
        Permanent aura = attachAura(creature);
        harness.setHand(player1, List.of(new KrosanGrip()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Aspect of Mongoose");
        harness.assertNotInGraveyard(player1, "Aspect of Mongoose");
        harness.assertInHand(player1, "Aspect of Mongoose");
    }

    @Test
    @DisplayName("Aspect of Mongoose returns to its owner's hand when controlled by another player")
    void returnsToOwnersHandWhenControlledByAnotherPlayer() {
        Permanent creature = addCreature(player2);
        AspectOfMongoose auraCard = new AspectOfMongoose();
        auraCard.setOwnerId(player1.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, auraCard);
        aura.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new KrosanGrip()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, aura.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Aspect of Mongoose");
        harness.assertNotInGraveyard(player1, "Aspect of Mongoose");
        harness.assertInHand(player1, "Aspect of Mongoose");
    }

    @Test
    @DisplayName("The player controlling Aspect of Mongoose controls its graveyard trigger")
    void lastControllerControlsReturnTrigger() {
        Permanent creature = addCreature(player2);
        AspectOfMongoose auraCard = new AspectOfMongoose();
        auraCard.setOwnerId(player1.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, auraCard);
        aura.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new KrosanGrip()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, aura.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        harness.assertInGraveyard(player1, "Aspect of Mongoose");
        resolveAllTriggers();
        harness.assertInHand(player1, "Aspect of Mongoose");
        harness.assertNotInHand(player2, "Aspect of Mongoose");
    }

    @Test
    @DisplayName("Aspect of Mongoose returns when its enchanted creature dies in combat")
    void returnsWhenEnchantedCreatureDies() {
        Permanent creature = addCreature();
        attachAura(creature);
        addCreature(player2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Ashcoat Bear");
        harness.assertNotOnBattlefield(player1, "Aspect of Mongoose");
        harness.assertNotInGraveyard(player1, "Aspect of Mongoose");
        harness.assertInHand(player1, "Aspect of Mongoose");
    }

    @Test
    @DisplayName("Bouncing the Aura removes shroud without a graveyard trigger")
    void bouncingAuraRemovesShroudWithoutTriggering() {
        Permanent creature = addCreature();
        Permanent aura = attachAura(creature);
        harness.setHand(player1, List.of(new WipeAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, aura.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Aspect of Mongoose");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    private Permanent addCreature() {
        return addCreature(player1);
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new AshcoatBear());
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AspectOfMongoose());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
