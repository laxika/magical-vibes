package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.Apocalypse;
import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.cards.e.Execute;
import com.github.laxika.magicalvibes.cards.m.MossfireEgg;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TravelingPlague.class, AvenFlock.class, Execute.class, MossfireEgg.class, Apocalypse.class,
        PlanarCleansing.class})
class TravelingPlagueTest extends BaseCardTest {

    @Test
    @DisplayName("Adds a plague counter on every upkeep and gives the enchanted creature -1/-1 per counter")
    void addsCountersOnEveryUpkeepAndShrinksEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new AvenFlock());
        castTravelingPlagueOn(player1, creature);
        Permanent plague = findPermanent(player1, "Traveling Plague");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(plague.getCounterCount(CounterType.PLAGUE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(plague.getCounterCount(CounterType.PLAGUE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns without counters and lets the former creature controller choose the new creature")
    void returnsWithoutCountersAndFormerControllerChoosesCreature() {
        Permanent oldCreature = addCreatureReady(player2, new AvenFlock());
        Permanent playerOneCreature = addCreatureReady(player1, new AvenFlock());
        Permanent playerTwoCreature = addCreatureReady(player2, new AvenFlock());
        castTravelingPlagueOn(player1, oldCreature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        Permanent plague = findPermanent(player1, "Traveling Plague");
        assertThat(plague.getCounterCount(CounterType.PLAGUE)).isEqualTo(1);

        destroyCreature(player1, oldCreature);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPermanentIds()).contains(playerOneCreature.getId(), playerTwoCreature.getId());

        harness.handlePermanentChosen(player2, playerOneCreature.getId());

        Permanent returnedPlague = findPermanent(player2, "Traveling Plague");
        assertThat(returnedPlague.getAttachedTo()).isEqualTo(playerOneCreature.getId());
        assertThat(returnedPlague.getCounterCount(CounterType.PLAGUE)).isZero();
        harness.assertNotInGraveyard(player1, "Traveling Plague");
        harness.assertNotOnBattlefield(player1, "Traveling Plague");
    }

    @Test
    @DisplayName("Stays in its owner's graveyard when no legal creature remains")
    void staysInGraveyardWithoutLegalCreature() {
        Permanent oldCreature = addCreatureReady(player2, new AvenFlock());
        castTravelingPlagueOn(player1, oldCreature);

        destroyCreature(player1, oldCreature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Traveling Plague");
        harness.assertNotOnBattlefield(player1, "Traveling Plague");
        harness.assertNotOnBattlefield(player2, "Traveling Plague");
    }

    @Test
    @DisplayName("Returns when the enchanted creature leaves for its owner's hand")
    void returnsWhenEnchantedCreatureReturnsToHand() {
        Permanent oldCreature = addCreatureReady(player2, new AvenFlock());
        Permanent replacementCreature = addCreatureReady(player1, new AvenFlock());
        castTravelingPlagueOn(player1, oldCreature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, oldCreature));
        resolveAllTriggers();

        Permanent returnedPlague = findPermanent(player2, "Traveling Plague");
        assertThat(returnedPlague.getAttachedTo()).isEqualTo(replacementCreature.getId());
        assertThat(returnedPlague.getCounterCount(CounterType.PLAGUE)).isZero();
        harness.assertInHand(player2, "Aven Flock");
        harness.assertNotInGraveyard(player1, "Traveling Plague");
    }

    @Test
    @DisplayName("Returns under the dying creature controller's control after plague counters kill it")
    void returnsAfterCountersReduceToughnessToZero() {
        Permanent oldCreature = addCreatureReady(player2, new AvenFlock());
        Permanent replacementCreature = addCreatureReady(player1, new AvenFlock());
        castTravelingPlagueOn(player1, oldCreature);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        advanceToUpkeep(player2);
        resolveAllTriggers();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Aven Flock");
        Permanent returnedPlague = findPermanent(player2, "Traveling Plague");
        assertThat(returnedPlague.getAttachedTo()).isEqualTo(replacementCreature.getId());
        assertThat(returnedPlague.getCounterCount(CounterType.PLAGUE)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, replacementCreature)).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Traveling Plague");
    }

    @Test
    @DisplayName("Does not return if the Aura is no longer in the graveyard when the trigger resolves")
    void doesNotReturnWhenRemovedFromGraveyardBeforeResolution() {
        Permanent oldCreature = addCreatureReady(player2, new AvenFlock());
        addCreatureReady(player1, new AvenFlock());
        castTravelingPlagueOn(player1, oldCreature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, oldCreature));
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Traveling Plague");
        harness.setGraveyard(player1, List.of());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Traveling Plague");
        harness.assertNotOnBattlefield(player2, "Traveling Plague");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Can enchant only a creature")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MossfireEgg());
        harness.setHand(player1, List.of(new TravelingPlague()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An Aura exiled as its host left isn't returned from a graveyard it reaches later")
    void doesNotReturnAnAuraExiledWithItsHostAndPutIntoAGraveyardLater() {
        Permanent host = addCreatureReady(player2, new AvenFlock());
        castTravelingPlagueOn(player1, host);
        Card aura = findPermanent(player1, "Traveling Plague").getOriginalCard();
        castSorceryForMana(new Apocalypse(), ManaColor.RED);
        assertThat(gd.findExiledCard(aura.getId())).isNotNull();

        gd.removeFromExile(aura.getId());
        putIntoGraveyard(aura);
        Permanent otherCreature = addCreatureReady(player2, new AvenFlock());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Traveling Plague");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(otherCreature);
    }

    @Test
    @DisplayName("An Aura destroyed with its host is returned")
    void returnsAnAuraDestroyedWithItsHost() {
        Permanent host = addCreatureReady(player2, new AvenFlock());
        castTravelingPlagueOn(player1, host);
        castSorceryForMana(new PlanarCleansing(), ManaColor.WHITE);

        Permanent otherCreature = addCreatureReady(player2, new AvenFlock());
        resolveAllTriggers();

        assertThat(findPermanent(player2, "Traveling Plague").getAttachedTo()).isEqualTo(otherCreature.getId());
    }

    @Test
    @DisplayName("An Aura destroyed with its host isn't returned after it left the graveyard and came back")
    void doesNotReturnAnAuraDestroyedWithItsHostThatCameBack() {
        Permanent host = addCreatureReady(player2, new AvenFlock());
        castTravelingPlagueOn(player1, host);
        Card aura = findPermanent(player1, "Traveling Plague").getOriginalCard();
        castSorceryForMana(new PlanarCleansing(), ManaColor.WHITE);

        gd.playerGraveyards.get(player1.getId()).remove(aura);
        gd.markGraveyardExit(aura.getId());
        putIntoGraveyard(aura);
        Permanent otherCreature = addCreatureReady(player2, new AvenFlock());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Traveling Plague");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(otherCreature);
    }

    @Test
    @DisplayName("An Aura the state-based check puts into the graveyard after a replacement question is returned")
    void returnsAnAuraPutThereByTheStateBasedBatch() {
        Permanent host = addCreatureReady(player2, new AvenFlock());
        Permanent otherCreature = addCreatureReady(player2, new AvenFlock());
        castTravelingPlagueOn(player1, host);
        Permanent plague = findPermanent(player1, "Traveling Plague");
        com.github.laxika.magicalvibes.testutil.TestCards.mutableCard(plague).addStaticEffect(
                new com.github.laxika.magicalvibes.model.effect.PutOnTopOfLibraryInsteadOfDyingEffect(true));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, host));
        harness.runStateBasedActions();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(findPermanent(player2, "Traveling Plague").getAttachedTo()).isEqualTo(otherCreature.getId());
    }

    private void castSorceryForMana(Card sorcery, ManaColor color) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(sorcery));
        harness.addMana(player1, color, 6);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void putIntoGraveyard(Card card) {
        gd.playerGraveyards.get(player1.getId()).add(card);
        gd.markGraveyardEntry(card);
    }

    private void castTravelingPlagueOn(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new TravelingPlague()));
        harness.addMana(caster, ManaColor.BLACK, 5);
        harness.castEnchantment(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    private void destroyCreature(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Execute()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }

    @Test
    @DisplayName("Does not return an Aura that left the graveyard and came back")
    void doesNotReturnAnAuraThatLeftTheGraveyardAndCameBack() {
        Permanent oldCreature = addCreatureReady(player2, new AvenFlock());
        addCreatureReady(player1, new AvenFlock());
        castTravelingPlagueOn(player1, oldCreature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, oldCreature));
        harness.runStateBasedActions();
        gd.playerGraveyards.get(player1.getId()).forEach(card -> gd.markGraveyardExit(card.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Traveling Plague");
        harness.assertNotOnBattlefield(player2, "Traveling Plague");
    }
}
