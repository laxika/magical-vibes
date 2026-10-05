package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.h.HerosDownfall;
import com.github.laxika.magicalvibes.cards.p.PersistentSpecimen;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OliviaCrimsonBride.class, PersistentSpecimen.class, HerosDownfall.class})
class OliviaCrimsonBrideTest extends BaseCardTest {

    @Test
    void returnsACreatureTappedAndAttacking() {
        addReadyOlivia();
        Card returnedCard = new PersistentSpecimen();
        harness.setGraveyard(player1, List.of(returnedCard));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(returnedCard.getId()));
        resolveAttackTrigger();

        Permanent returned = findPermanent(player1, "Persistent Specimen");
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttacking()).isTrue();
        assertThat(returned.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(returned.isAttackedThisTurn()).isFalse();
        assertThat(returned.getAttacksThisTurn()).isZero();
        assertThat(returned.getAttacksThisGame()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exilesReturnedCreatureWhenNoLegendaryVampireIsControlled() {
        Permanent olivia = addReadyOlivia();
        Card returnedCard = new PersistentSpecimen();
        harness.setGraveyard(player1, List.of(returnedCard));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(returnedCard.getId()));
        resolveAttackTrigger();

        harness.setHand(player1, List.of(new HerosDownfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, olivia.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId())
                .isEqualTo(findPermanent(player1, "Persistent Specimen").getId());

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(returnedCard.getId()));
    }

    @Test
    void offersOnlyCreatureCardsInItsControllersGraveyard() {
        addReadyOlivia();
        Card creature = new PersistentSpecimen();
        Card instant = new HerosDownfall();
        Card opponentsCreature = new PersistentSpecimen();
        harness.setGraveyard(player1, List.of(creature, instant));
        harness.setGraveyard(player2, List.of(opponentsCreature));

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactly(creature);
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        resolveAttackTrigger();

        harness.assertOnBattlefield(player1, "Persistent Specimen");
        harness.assertInGraveyard(player1, "Hero's Downfall");
        harness.assertInGraveyard(player2, "Persistent Specimen");
    }

    @Test
    void doesNotReturnATargetThatLeftTheGraveyardBeforeResolution() {
        addReadyOlivia();
        Card target = new PersistentSpecimen();
        harness.setGraveyard(player1, List.of(target));
        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(target));
        resolveAttackTrigger();

        harness.assertNotOnBattlefield(player1, "Persistent Specimen");
        harness.assertInHand(player1, "Persistent Specimen");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsThenExilesCreatureIfOliviaLeavesBeforeTheAttackTriggerResolves() {
        Permanent olivia = addReadyOlivia();
        Card target = new PersistentSpecimen();
        harness.setGraveyard(player1, List.of(target));
        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setHand(player1, List.of(new HerosDownfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, olivia.getId());
        harness.assertNotOnBattlefield(player1, "Olivia, Crimson Bride");
        resolveAttackTrigger();

        harness.assertOnBattlefield(player1, "Persistent Specimen");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Persistent Specimen");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    void aReturnedLegendaryVampireSatisfiesItsOwnGrantedAbility() {
        Permanent olivia = addReadyOlivia();
        Card target = new OliviaCrimsonBride();
        harness.setGraveyard(player1, List.of(target));
        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setHand(player1, List.of(new HerosDownfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, olivia.getId());
        resolveAttackTrigger();

        assertThat(findPermanent(player1, "Olivia, Crimson Bride").getCard().getId())
                .isEqualTo(target.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void anOpponentsLegendaryVampireDoesNotPreventTheExileTrigger() {
        Permanent olivia = addReadyOlivia();
        addCreatureReady(player2, new OliviaCrimsonBride());
        Card target = new PersistentSpecimen();
        harness.setGraveyard(player1, List.of(target));
        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAttackTrigger();

        harness.setHand(player1, List.of(new HerosDownfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, olivia.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Olivia, Crimson Bride");
        harness.assertNotOnBattlefield(player1, "Persistent Specimen");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    void regainingALegendaryVampireDoesNotCancelAnAlreadyTriggeredExile() {
        Permanent olivia = addReadyOlivia();
        Card target = new PersistentSpecimen();
        harness.setGraveyard(player1, List.of(target));
        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAttackTrigger();

        harness.setHand(player1, List.of(new HerosDownfall()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, olivia.getId());
        assertThat(gd.stack).hasSize(1);
        addReadyOlivia();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Olivia, Crimson Bride");
        harness.assertNotOnBattlefield(player1, "Persistent Specimen");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }

    private Permanent addReadyOlivia() {
        return addCreatureReady(player1, new OliviaCrimsonBride());
    }

    private void resolveAttackTrigger() {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);
    }
}
