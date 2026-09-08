package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PuppetMasterTest extends BaseCardTest {

    @Test
    @DisplayName("When enchanted creature dies, it returns to its owner's hand")
    void returnsCreatureToOwnersHand() {
        Permanent creature = addPuppetMaster(player2, player1);
        Card creatureCard = creature.getCard();

        destroyCreature(player2, creature);

        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(creatureCard.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(creatureCard.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Paying returns Puppet Master to its owner's hand")
    void payingReturnsAuraToHand() {
        Permanent creature = addPuppetMaster(player2, player1);
        destroyCreature(player2, creature);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Puppet Master");
        harness.assertNotInGraveyard(player1, "Puppet Master");
    }

    @Test
    @DisplayName("Declining the payment leaves Puppet Master in its owner's graveyard")
    void decliningPaymentLeavesAuraInGraveyard() {
        Permanent creature = addPuppetMaster(player2, player1);
        destroyCreature(player2, creature);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Puppet Master");
        harness.assertNotInHand(player1, "Puppet Master");
    }

    private Permanent addPuppetMaster(Player creatureController, Player auraController) {
        harness.addToBattlefield(creatureController, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(creatureController.getId()).getFirst();

        Permanent aura = new Permanent(new PuppetMaster());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
        return creature;
    }

    private void destroyCreature(Player creatureController, Permanent creature) {
        harness.forceActivePlayer(creatureController == player1 ? player2 : player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Player caster = creatureController == player1 ? player2 : player1;
        harness.setHand(caster, List.of(new DarkBanishing()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        harness.castInstant(caster, 0, creature.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.passBothPriorities();
    }
}
