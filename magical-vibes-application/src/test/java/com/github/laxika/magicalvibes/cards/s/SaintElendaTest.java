package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FaithsFetters;
import com.github.laxika.magicalvibes.cards.i.InspiringCleric;
import com.github.laxika.magicalvibes.cards.i.InvokeTheDivine;
import com.github.laxika.magicalvibes.cards.r.RitualOfRejuvenation;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaintElenda.class, FaithsFetters.class, InvokeTheDivine.class,
        RitualOfRejuvenation.class, InspiringCleric.class})
class SaintElendaTest extends BaseCardTest {

    @Test
    void entersAndOffersAFreeCastFromItsSpellbookToItsController() {
        harness.setHand(player1, java.util.List.of(new SaintElenda()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.SpellbookCardChoice choice =
                (PendingInteraction.SpellbookCardChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.cards()).hasSize(3);

        Card selected = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, java.util.List.of(selected.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(selected.getId()));
    }

    @Test
    void createsAnAvatarWhosePowerAndToughnessEqualLifeGained() {
        harness.addToBattlefield(player1, new SaintElenda());
        gd.lifeGainedThisTurn.put(player1.getId(), 4);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        var avatars = findPermanents(player1, "Avatar");
        assertThat(avatars).hasSize(1);
        assertThat(avatars.getFirst().getCard().getPower()).isEqualTo(4);
        assertThat(avatars.getFirst().getCard().getToughness()).isEqualTo(4);
        assertThat(avatars.getFirst().getCard().isToken()).isTrue();
    }

    @Test
    void createsNoAvatarWithoutLifeGain() {
        harness.addToBattlefield(player1, new SaintElenda());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Avatar")).isEmpty();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
