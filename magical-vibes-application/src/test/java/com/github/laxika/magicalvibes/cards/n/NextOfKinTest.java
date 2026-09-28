package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedReturnAuraAttachedToPermanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NextOfKin.class, DoomBlade.class, HillGiant.class, GrizzlyBears.class})
class NextOfKinTest extends BaseCardTest {

    @Test
    @DisplayName("puts a lower-mana-value creature from hand and reattaches at the next end step")
    void putsCreatureFromHandAndReattachesAura() {
        Permanent dyingCreature = addCreatureReady(player1, new HillGiant());
        NextOfKin aura = new NextOfKin();
        Card replacement = new GrizzlyBears();
        castAura(dyingCreature, aura);

        destroyDyingCreature(dyingCreature.getId(), replacement);

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(replacement.getId());
        assertThat(choice.includeGraveyard()).isFalse();
        assertThat(choice.includeCommandZone()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(replacement.getId()));
        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(gd.getDelayedActions(DelayedReturnAuraAttachedToPermanent.class))
                .contains(new DelayedReturnAuraAttachedToPermanent(
                        aura.getId(), player1.getId(), entered.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura);

        returnAuraAtNextEndStep();
        Permanent returnedAura = findPermanent(player1, "Next of Kin");
        assertThat(returnedAura.getAttachedTo()).isEqualTo(entered.getId());
    }

    @Test
    @DisplayName("can put a lower-mana-value creature from the command zone")
    void putsCreatureFromCommandZoneAndReattachesAura() {
        Permanent dyingCreature = addCreatureReady(player1, new HillGiant());
        NextOfKin aura = new NextOfKin();
        Card replacement = new GrizzlyBears();
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(replacement)));
        castAura(dyingCreature, aura);

        destroyDyingCreature(dyingCreature.getId(), null);

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(replacement.getId());
        assertThat(gd.playerCommandZones.get(player1.getId())).containsExactly(replacement);

        harness.handleMultipleCardsChosen(player1, List.of(replacement.getId()));
        Permanent entered = findPermanent(player1, "Grizzly Bears");
        assertThat(gd.playerCommandZones.get(player1.getId())).isEmpty();
        assertThat(entered.getEnteredFromZone()).isEqualTo(com.github.laxika.magicalvibes.model.Zone.COMMAND);

        returnAuraAtNextEndStep();
        assertThat(findPermanent(player1, "Next of Kin").getAttachedTo()).isEqualTo(entered.getId());
    }

    private void castAura(Permanent creature, NextOfKin aura) {
        harness.setHand(player1, List.of(aura));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }

    private void destroyDyingCreature(java.util.UUID creatureId, Card replacement) {
        List<Card> hand = new ArrayList<>(List.of(new DoomBlade()));
        if (replacement != null) {
            hand.add(replacement);
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, creatureId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void returnAuraAtNextEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
