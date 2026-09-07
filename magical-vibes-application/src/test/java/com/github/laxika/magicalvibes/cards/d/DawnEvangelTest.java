package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnEvangel.class, DoomBlade.class, GrizzlyBears.class, HillGiant.class, Pacifism.class, Shock.class})
class DawnEvangelTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature card with mana value 2 or less when an Aura you control was attached")
    void returnsEligibleCreatureWhenControlledAuraWasAttached() {
        Permanent dyingCreature = addEnchantedCreature(player2, player1);
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive));

        killCreature(dyingCreature);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(eligible.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(eligible.getId()));
    }

    @Test
    @DisplayName("Does not trigger for a creature enchanted by an opponent's Aura")
    void doesNotTriggerForOpponentControlledAura() {
        Permanent dyingCreature = addEnchantedCreature(player2, player2);
        Card eligible = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible));

        killCreature(dyingCreature);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers when Dawn Evangel dies while enchanted by an Aura it controls")
    void triggersWhenDawnEvangelDiesEnchanted() {
        Permanent evangel = harness.addToBattlefieldAndReturn(player1, new DawnEvangel());
        attachAura(player1, evangel);
        Card eligible = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible));

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, evangel.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
    }

    private Permanent addEnchantedCreature(Player creatureController, Player auraController) {
        Permanent creature = harness.addToBattlefieldAndReturn(creatureController, new GrizzlyBears());
        attachAura(auraController, creature);
        harness.addToBattlefield(player1, new DawnEvangel());
        return creature;
    }

    private Permanent attachAura(Player auraController, Permanent creature) {
        Permanent aura = new Permanent(new Pacifism());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
        return aura;
    }

    private void killCreature(Permanent creature) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
