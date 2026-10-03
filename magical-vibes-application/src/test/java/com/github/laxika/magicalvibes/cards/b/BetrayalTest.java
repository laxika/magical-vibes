package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.RiverBoa;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Betrayal.class, RiverBoa.class})
class BetrayalTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast Betrayal targeting a creature an opponent controls")
    void canTargetOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RiverBoa());
        harness.setHand(player1, List.of(new Betrayal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cannot cast Betrayal targeting a creature you control")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player2, new RiverBoa());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new RiverBoa());
        harness.setHand(player1, List.of(new Betrayal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, own.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Resolving Betrayal attaches it to the target creature")
    void resolvingAttaches() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RiverBoa());
        harness.setHand(player1, List.of(new Betrayal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Betrayal")
                        && creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Tapping the enchanted creature draws a card for the Aura's controller")
    void tappingEnchantedCreatureDraws() {
        Permanent creature = attachAura();
        harness.setLibrary(player1, List.of(new RiverBoa()));
        int auraControllerHandBefore = gd.playerHands.get(player1.getId()).size();
        int enchantedCreatureControllerHandBefore = gd.playerHands.get(player2.getId()).size();

        creature.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, creature));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(auraControllerHandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(enchantedCreatureControllerHandBefore);
    }

    @Test
    @DisplayName("An un-enchanted creature becoming tapped draws nothing")
    void unenchantedCreatureDrawsNothing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RiverBoa());
        harness.setLibrary(player1, List.of(new RiverBoa()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        creature.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Attacking with the enchanted creature draws for the Aura's controller")
    void attackingEnchantedCreatureDraws() {
        Permanent creature = attachAura();
        creature.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new RiverBoa(), new RiverBoa()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Attaching Betrayal to an already tapped creature does not draw")
    void attachingToTappedCreatureDoesNotDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RiverBoa());
        creature.tap();
        harness.setLibrary(player1, List.of(new RiverBoa()));
        harness.setHand(player1, List.of(new Betrayal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Betrayal").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A queued draw still resolves after Betrayal leaves the battlefield")
    void drawSurvivesAuraLeavingBattlefield() {
        Permanent creature = attachAura();
        Permanent aura = findPermanent(player1, "Betrayal");
        harness.setLibrary(player1, List.of(new RiverBoa()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        creature.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, creature));
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    private Permanent attachAura() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RiverBoa());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Betrayal());
        aura.setAttachedTo(creature.getId());
        return creature;
    }
}
