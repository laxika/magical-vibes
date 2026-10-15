package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AncientGrudge;
import com.github.laxika.magicalvibes.cards.b.BurnFromWithin;
import com.github.laxika.magicalvibes.cards.d.DenyExistence;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.l.LightningAxe;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.MagmaticChasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarnessTheStorm.class, LightningBolt.class, Shock.class, AncientGrudge.class,
        FountainOfYouth.class, MagmaticChasm.class, BurnFromWithin.class, LightningAxe.class,
        HulkingDevil.class, DenyExistence.class})
class HarnessTheStormTest extends BaseCardTest {

    @Test
    @DisplayName("Targets a same-name instant or sorcery and casts it for its normal cost")
    void castsSameNameCardFromGraveyard() {
        LightningBolt graveyardBolt = new LightningBolt();
        Shock differentName = new Shock();
        harness.addToBattlefield(player1, new HarnessTheStorm());
        harness.setGraveyard(player1, List.of(graveyardBolt, differentName));
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(graveyardBolt.getId());

        harness.handleMultipleCardsChosen(player1, List.of(graveyardBolt.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Does not trigger for an instant or sorcery cast from a graveyard")
    void doesNotTriggerForGraveyardCast() {
        AncientGrudge grudge = new AncientGrudge();
        harness.addToBattlefield(player1, new HarnessTheStorm());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(grudge));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);

        harness.castFromGraveyardTargeting(player1, 0,
                harness.getPermanentId(player2, "Fountain of Youth"));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotTargetAnOpponentsGraveyard() {
        MagmaticChasm opponentCard = new MagmaticChasm();
        harness.addToBattlefield(player1, new HarnessTheStorm());
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.castFromHand(player1, new MagmaticChasm(), "{1}{R}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
    }

    @Test
    void doesNotTriggerForAnOpponentsSpell() {
        harness.addToBattlefield(player1, new HarnessTheStorm());
        harness.setGraveyard(player1, List.of(new MagmaticChasm()));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new MagmaticChasm(), "{1}{R}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void mayDeclineToCastTheTargetedCard() {
        MagmaticChasm graveyardCard = new MagmaticChasm();
        harness.addToBattlefield(player1, new HarnessTheStorm());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new MagmaticChasm()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void castsSorceryDuringResolutionBeforeTheOriginalSpell() {
        MagmaticChasm graveyardCard = new MagmaticChasm();
        MagmaticChasm handCard = new MagmaticChasm();
        harness.addToBattlefield(player1, new HarnessTheStorm());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(handCard));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getId()).isEqualTo(graveyardCard.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard).doesNotContain(handCard);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard, handCard);
    }

    @Test
    void cannotCastWithoutPayingTheManaCost() {
        MagmaticChasm graveyardCard = new MagmaticChasm();
        harness.addToBattlefield(player1, new HarnessTheStorm());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new MagmaticChasm()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void choosesANewNonzeroXForTheGraveyardSpell() {
        BurnFromWithin graveyardCard = new BurnFromWithin();
        harness.addToBattlefield(player1, new HarnessTheStorm());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new BurnFromWithin()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 3);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.passBothPriorities();
        harness.assertLife(player2, 15);
    }

    @Test
    void cannotSkipLightningAxesMandatoryAdditionalCost() {
        LightningAxe graveyardCard = new LightningAxe();
        MagmaticChasm discardCard = new MagmaticChasm();
        harness.addToBattlefield(player1, new HarnessTheStorm());
        harness.addToBattlefield(player2, new HulkingDevil());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new LightningAxe(), new MagmaticChasm(), discardCard));
        harness.addMana(player1, ManaColor.RED, 2);
        var targetId = harness.getPermanentId(player2, "Hulking Devil");

        harness.castInstantWithDiscard(player1, 0, targetId, 1);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, targetId);
        }

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(graveyardCard.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(discardCard);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    @Test
    void castsACounterspellFromTheGraveyardTargetingASpellOnTheStack() {
        HulkingDevil creatureSpell = new HulkingDevil();
        DenyExistence graveyardCard = new DenyExistence();
        harness.addToBattlefield(player1, new HarnessTheStorm());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, creatureSpell, "{3}{R}");
        harness.setHand(player1, List.of(new DenyExistence()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castInstant(player1, 0, creatureSpell.getId());
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creatureSpell.getId());

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack.getLast().getCard().getId()).isEqualTo(graveyardCard.getId());
        harness.assertNotInGraveyard(player1, "Deny Existence");
    }
}
