package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GristleGrinner;
import com.github.laxika.magicalvibes.cards.k.KrovikanScoundrel;
import com.github.laxika.magicalvibes.cards.m.MishrasBauble;
import com.github.laxika.magicalvibes.cards.o.Ovinize;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoidMaw.class, KrovikanScoundrel.class, MishrasBauble.class, GristleGrinner.class, Ovinize.class})
class VoidMawTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature is exiled with Void Maw instead of entering a graveyard")
    void exilesAnotherCreatureWithSource() {
        Permanent maw = addMaw();
        Permanent scoundrel = harness.addToBattlefieldAndReturn(player1, new KrovikanScoundrel());
        Permanent bauble = harness.addToBattlefieldAndReturn(player1, new MishrasBauble());

        removeToGraveyard(scoundrel);
        removeToGraveyard(bauble);

        assertThat(gd.getCardsExiledByPermanent(maw.getId()))
                .extracting(Card::getId).containsExactly(scoundrel.getCard().getId());
        harness.assertNotInGraveyard(player1, "Krovikan Scoundrel");
        harness.assertInGraveyard(player1, "Mishra's Bauble");
    }

    @Test
    @DisplayName("Void Maw itself is not exiled by its replacement effect")
    void doesNotExileItself() {
        Permanent maw = addMaw();

        removeToGraveyard(maw);

        harness.assertInGraveyard(player1, "Void Maw");
        assertThat(gd.getCardsExiledByPermanent(maw.getId())).isEmpty();
    }

    @Test
    @DisplayName("Putting an exiled card into its owner's graveyard pays for the self-boost")
    void paysWithExiledCardAndBoostsUntilEndOfTurn() {
        Permanent maw = addMaw();
        Permanent scoundrel = harness.addToBattlefieldAndReturn(player1, new KrovikanScoundrel());
        removeToGraveyard(scoundrel);
        UUID exiledCardId = gd.getCardsExiledByPermanent(maw.getId()).getFirst().getId();

        int mawIndex = gd.playerBattlefields.get(player1.getId()).indexOf(maw);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, mawIndex, 0, exiledCardId, Zone.EXILE);

        assertThat(gd.findExiledCard(exiledCardId)).isNull();
        harness.assertInGraveyard(player1, "Krovikan Scoundrel");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, maw)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, maw)).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, maw)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, maw)).isEqualTo(5);
    }

    @Test
    @DisplayName("Activated ability is available only while a card is exiled with Void Maw")
    void abilityAvailabilityRequiresExiledCard() {
        Permanent maw = addMaw();

        assertThat(gs.canActivateAbility(gd, player1.getId(), maw, 0,
                gd.playerManaPools.get(player1.getId()))).isFalse();

        Permanent scoundrel = harness.addToBattlefieldAndReturn(player1, new KrovikanScoundrel());
        removeToGraveyard(scoundrel);

        assertThat(gs.canActivateAbility(gd, player1.getId(), maw, 0,
                gd.playerManaPools.get(player1.getId()))).isTrue();
        assertThat(gd.getCardsExiledByPermanent(maw.getId()))
                .extracting(Card::getId)
                .containsExactly(scoundrel.getCard().getId());
    }

    @Test
    @DisplayName("Chooses one of multiple cards exiled with Void Maw and returns it to its owner's graveyard")
    void choosesOneOfMultipleExiledCards() {
        Permanent maw = addMaw();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new KrovikanScoundrel());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new KrovikanScoundrel());
        removeToGraveyard(first);
        removeToGraveyard(second);

        int mawIndex = gd.playerBattlefields.get(player1.getId()).indexOf(maw);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, mawIndex, 0, null, null);

        PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                first.getCard().getId(), second.getCard().getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getCard().getId()));

        assertThat(gd.findExiledCard(first.getCard().getId())).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId).contains(first.getCard().getId());
        assertThat(gd.getCardsExiledByPermanent(maw.getId()))
                .extracting(Card::getId).containsExactly(second.getCard().getId());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, maw)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, maw)).isEqualTo(7);
    }

    @Test
    @DisplayName("Exiling a creature instead of letting it die does not trigger dies abilities")
    void exileReplacementDoesNotTriggerCreatureDiesAbilities() {
        Permanent maw = addMaw();
        Permanent grinner = harness.addToBattlefieldAndReturn(player1, new GristleGrinner());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new KrovikanScoundrel());
        int grinnerPower = gqs.getEffectivePower(gd, grinner);

        removeToGraveyard(victim);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, grinner)).isEqualTo(grinnerPower);
        assertThat(gd.getCardsExiledByPermanent(maw.getId()))
                .extracting(Card::getId).containsExactly(victim.getCard().getId());
    }

    @Test
    @DisplayName("Void Maw without abilities does not replace another creature's death")
    void abilityLossDisablesExileReplacement() {
        Permanent maw = addMaw();
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new KrovikanScoundrel());
        harness.setHand(player1, List.of(new Ovinize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, maw.getId());
        harness.passBothPriorities();

        removeToGraveyard(victim);

        harness.assertInGraveyard(player2, "Krovikan Scoundrel");
        assertThat(gd.findExiledCard(victim.getCard().getId())).isNull();
    }

    @Test
    @DisplayName("Void Maw replaces another creature's death even when both have lethal damage")
    void exilesCreatureDyingSimultaneouslyWithMaw() {
        Permanent maw = addMaw();
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new KrovikanScoundrel());
        maw.setMarkedDamage(5);
        victim.setMarkedDamage(1);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Void Maw");
        harness.assertNotInGraveyard(player1, "Krovikan Scoundrel");
        assertThat(gd.findExiledCard(victim.getCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("A dying creature's controller chooses which of two Void Maws exiles it")
    void competingMawsRequireReplacementChoice() {
        addMaw();
        harness.addToBattlefield(player2, new VoidMaw());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new KrovikanScoundrel());

        removeToGraveyard(victim);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.findExiledCard(victim.getCard().getId())).isNull();
    }

    @Test
    @DisplayName("Returning a card from exile as a cost does not trigger creature dies abilities")
    void payingCostDoesNotCountAsDying() {
        Permanent maw = addMaw();
        Permanent grinner = harness.addToBattlefieldAndReturn(player1, new GristleGrinner());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new KrovikanScoundrel());
        removeToGraveyard(victim);
        int grinnerPower = gqs.getEffectivePower(gd, grinner);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(maw),
                0, victim.getCard().getId(), Zone.EXILE);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Krovikan Scoundrel");
        assertThat(gqs.getEffectivePower(gd, grinner)).isEqualTo(grinnerPower);
        assertThat(gqs.getEffectivePower(gd, maw)).isEqualTo(6);
    }

    private Permanent addMaw() {
        return addCreatureReady(player1, new VoidMaw());
    }

    private void removeToGraveyard(Permanent permanent) {
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
    }
}
