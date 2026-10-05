package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.e.ExpeditionChampion;
import com.github.laxika.magicalvibes.cards.e.EnormousEnergyBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NahiriHeirOfTheAncients.class, Bonesplitter.class, ChandraNalaar.class,
        ExpeditionChampion.class, Forest.class, HillGiant.class, LeoninScimitar.class, Shock.class, EnormousEnergyBlade.class})
class NahiriHeirOfTheAncientsTest extends BaseCardTest {

    @Test
    void createsKorWarriorAndMayAttachControlledEquipment() {
        Permanent nahiri = addReadyNahiri(player1, 4);
        Permanent bonesplitter = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Kor Warrior");
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.KOR, CardSubtype.WARRIOR);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bonesplitter.getId());

        assertThat(bonesplitter.getAttachedTo()).isEqualTo(token.getId());
        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void findsOneWarriorOrEquipmentAndRandomizesTheRestToTheBottom() {
        Permanent nahiri = addReadyNahiri(player1, 3);
        Card forest = new Forest();
        Card shock = new Shock();
        Card warrior = new ExpeditionChampion();
        Card equipment = new LeoninScimitar();
        Card hillGiant = new HillGiant();
        Card secondShock = new Shock();
        harness.setLibrary(player1, List.of(forest, shock, warrior, equipment, hillGiant, secondShock));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(warrior.getId(), equipment.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(warrior.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(warrior);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                forest, shock, equipment, hillGiant, secondShock);
        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void dealsTwiceTheNumberOfControlledEquipmentToCreature() {
        Permanent nahiri = addReadyNahiri(player1, 5);
        harness.addToBattlefield(player1, new Bonesplitter());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player2, new Bonesplitter());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void dealsTwiceTheNumberOfControlledEquipmentToPlaneswalker() {
        Permanent nahiri = addReadyNahiri(player1, 5);
        harness.addToBattlefield(player1, new Bonesplitter());
        harness.addToBattlefield(player1, new LeoninScimitar());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, 2, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void attachingEquipmentTriggersItsBecomesAttachedAbility() {
        addReadyNahiri(player1, 4);
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new EnormousEnergyBlade());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Kor Warrior");
        harness.handlePermanentChosen(player1, blade.getId());

        assertThat(blade.getAttachedTo()).isEqualTo(token.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(token.isTapped()).isTrue();
    }

    @Test
    void mayDeclineOnlyEligibleCardInShortLibrary() {
        addReadyNahiri(player1, 4);
        Card warrior = new ExpeditionChampion();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(warrior, forest));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(warrior, forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void noEligibleCardsInShortLibraryMoveToBottomWithoutChoice() {
        addReadyNahiri(player1, 4);
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void createsTokenWithoutOfferingOpponentsEquipment() {
        addReadyNahiri(player1, 4);
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kor Warrior");
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayDeclineAttachingEquipmentAndPreservesExistingAttachment() {
        addReadyNahiri(player1, 4);
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ExpeditionChampion());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        equipment.setAttachedTo(warrior.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());

        harness.assertOnBattlefield(player1, "Kor Warrior");
        assertThat(equipment.getAttachedTo()).isEqualTo(warrior.getId());
    }

    @Test
    void damageCountsEquipmentAtResolutionAfterNahiriDiesToLoyaltyCost() {
        addReadyNahiri(player1, 3);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        Permanent warrior = harness.addToBattlefieldAndReturn(player2, new ExpeditionChampion());

        harness.activateAbility(player1, 0, 2, null, warrior.getId());
        harness.assertInGraveyard(player1, "Nahiri, Heir of the Ancients");
        gd.playerBattlefields.get(player1.getId()).remove(equipment);
        harness.passBothPriorities();

        assertThat(warrior.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Expedition Champion");
    }

    @Test
    void choosesEquipmentAndLeavesUnexaminedCardsAboveTheRest() {
        addReadyNahiri(player1, 4);
        Card equipment = new LeoninScimitar();
        List<Card> examined = List.of(equipment, new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        Card unexamined = new ExpeditionChampion();
        ArrayList<Card> library = new ArrayList<>(examined);
        library.add(unexamined);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(equipment);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unexamined);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(examined.subList(1, 6));
    }

    @Test
    void emptyLibraryDoesNotRequireAChoice() {
        addReadyNahiri(player1, 4);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyNahiri(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new NahiriHeirOfTheAncients());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
