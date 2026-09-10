package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.d.DocOcksHenchmen;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordsmanSharpScoundrel.class, DocOcksHenchmen.class, GrizzlyBears.class,
        LeoninScimitar.class, Mountain.class})
class SwordsmanSharpScoundrelTest extends BaseCardTest {

    @Test
    @DisplayName("Attaches a target Equipment to a target creature when another Villain enters")
    void attachesEquipmentToCreatureWhenVillainEnters() {
        addCreatureReady(player1, new SwordsmanSharpScoundrel());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DocOcksHenchmen()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Does not attach Equipment when a non-Villain enters")
    void doesNotTriggerForNonVillain() {
        addCreatureReady(player1, new SwordsmanSharpScoundrel());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(creature.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An equipped creature you control connives when it attacks")
    void equippedCreatureConnivesWhenItAttacks() {
        Permanent swordsman = addCreatureReady(player1, new SwordsmanSharpScoundrel());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(swordsman.getId());
        Card discarded = new GrizzlyBears();
        Card kept = new Mountain();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(discarded));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(discarded));

        assertThat(swordsman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
    }

    @Test
    @DisplayName("Does not trigger for an equipped creature controlled by an opponent")
    void doesNotTriggerForOpponentsEquippedCreature() {
        addCreatureReady(player1, new SwordsmanSharpScoundrel());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.setAttachedTo(attacker.getId());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard() instanceof SwordsmanSharpScoundrel);
    }
}
