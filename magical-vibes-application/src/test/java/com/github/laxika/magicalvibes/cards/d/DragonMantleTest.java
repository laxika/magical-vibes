package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
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

@CardUsed({DragonMantle.class, TravelingPhilosopher.class, Forest.class, Mountain.class})
class DragonMantleTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Dragon Mantle attaches to a creature and draws a card")
    void resolvingAttachesAndDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new DragonMantle()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castEnchantment(player1, 0, List.of(creature.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> creature.getId().equals(permanent.getAttachedTo()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Enchanted creature can activate {R}: +1/+0")
    void grantedAbilityBoostsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new TravelingPhilosopher());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DragonMantle());
        aura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Dragon Mantle cannot enchant a land")
    void cannotEnchantALand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new DragonMantle()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent mountain = gd.playerBattlefields.get(player1.getId()).getFirst();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Repeated activations work while summoning sick and tapped and expire at cleanup")
    void repeatedActivationsExpire() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        creature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DragonMantle());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanting an opponent's creature draws for the Aura controller and grants the creature's controller the ability")
    void opponentControlsGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new DragonMantle()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("An illegal target prevents Dragon Mantle from entering and drawing")
    void removedTargetPreventsDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new DragonMantle()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof DragonMantle);
    }

    @Test
    @DisplayName("Removing the Aura does not stop an already activated ability")
    void activatedAbilitySurvivesAuraRemoval() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DragonMantle());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
