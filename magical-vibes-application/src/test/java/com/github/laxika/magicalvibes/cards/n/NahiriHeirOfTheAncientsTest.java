package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.e.ExpeditionChampion;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NahiriHeirOfTheAncients.class, Bonesplitter.class, ChandraNalaar.class,
        ExpeditionChampion.class, Forest.class, HillGiant.class, LeoninScimitar.class, Shock.class})
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

    private Permanent addReadyNahiri(Player player, int loyalty) {
        Permanent permanent = new Permanent(new NahiriHeirOfTheAncients());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
