package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernalScarring.class, GrizzlyBears.class, Shock.class})
class InfernalScarringTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+0")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Controller draws a card when the enchanted creature dies")
    void controllerDrawsWhenEnchantedCreatureDies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, creature);
        int handSize = gd.playerHands.get(player1.getId()).size();

        killWithShock(creature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("No card is drawn when an unenchanted creature dies")
    void noDrawWhenUnenchantedCreatureDies() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, enchanted);
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        int handSize = gd.playerHands.get(player1.getId()).size();

        killWithShock(other);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("The creature's controller draws, rather than the Aura's controller")
    void creatureControllerDrawsWithOpponentsAura() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player2, creature);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        killWithShock(creature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
    }

    @Test
    @DisplayName("Two Auras grant two death triggers and their power bonuses stack")
    void multipleAurasEachGrantDeathTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, creature);
        attachAura(player2, creature);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        killWithShock(creature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting the Aura on an opposing creature grants its bonus and death trigger")
    void auraResolvesOnOpposingCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new InfernalScarring()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castEnchantment(player2, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player2, "Infernal Scarring").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        killWithShock(creature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof InfernalScarring);
    }

    private void killWithShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();
    }

    private void attachAura(Player auraController, Permanent creature) {
        Permanent aura = new Permanent(new InfernalScarring());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
    }
}
