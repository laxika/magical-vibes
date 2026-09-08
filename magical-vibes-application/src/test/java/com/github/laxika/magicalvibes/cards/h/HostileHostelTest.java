package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CreepingInn;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({HostileHostel.class, CreepingInn.class, GrizzlyBears.class, Island.class})
class HostileHostelTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Hostile Hostel adds colorless mana")
    void tapsForColorlessMana() {
        Permanent hostel = addReadyHostel(player1);

        harness.activateAbility(player1, indexOf(player1, hostel), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(hostel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The soul ability sacrifices a creature and adds a soul counter")
    void addsSoulCounterAfterSacrificingCreature() {
        Permanent hostel = addReadyHostel(player1);
        addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, hostel), 1, null, null);
        harness.passBothPriorities();

        assertThat(hostel.getCounterCount(CounterType.SOUL)).isOne();
        assertThat(hostel.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The third soul counter transforms the land and untaps it")
    void thirdSoulCounterTransformsAndUntaps() {
        Permanent hostel = addReadyHostel(player1);
        hostel.setCounterCount(CounterType.SOUL, 2);
        addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, hostel), 1, null, null);
        harness.passBothPriorities();

        assertThat(hostel.getCard().getName()).isEqualTo("Creeping Inn");
        assertThat(hostel.isTransformed()).isTrue();
        assertThat(hostel.getCounterCount(CounterType.SOUL)).isZero();
        assertThat(hostel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The soul ability is sorcery speed")
    void soulAbilityRequiresSorcerySpeed() {
        Permanent hostel = addReadyHostel(player1);
        addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, hostel), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creeping Inn exiles a creature and drains each opponent by the number exiled with it")
    void attackTriggerUsesCreatureCardsExiledWithIt() {
        Permanent inn = addTransformedInn(player1);
        Card creature = new GrizzlyBears();
        Card nonCreature = new Island();
        harness.setGraveyard(player1, List.of(creature, nonCreature));
        harness.setLife(player1, 20);
        harness.setLife(player2, 23);

        attack(inn);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        resolveAllStack();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getCardsExiledByPermanent(inn.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonCreature);
    }

    @Test
    @DisplayName("Creeping Inn can phase out")
    void phasesOut() {
        Permanent inn = addTransformedInn(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, indexOf(player1, inn), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(inn);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(inn);
    }

    private Permanent addReadyHostel(Player player) {
        Permanent hostel = new Permanent(new HostileHostel());
        hostel.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(hostel);
        return hostel;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent addTransformedInn(Player player) {
        HostileHostel card = new HostileHostel();
        Permanent inn = new Permanent(card);
        inn.setCard(card.getBackFaceCard());
        inn.setTransformed(true);
        inn.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(inn);
        return inn;
    }

    private void attack(Permanent inn) {
        declareAttackers(player1, List.of(indexOf(player1, inn)));
        harness.passBothPriorities();
    }

    private void resolveAllStack() {
        int guard = 0;
        while ((!gd.stack.isEmpty() || gd.interaction.isAwaitingInput()) && guard++ < 50) {
            harness.passBothPriorities();
        }
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
