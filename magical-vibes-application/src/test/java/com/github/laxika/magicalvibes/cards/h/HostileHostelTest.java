package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CreepingInn;
import com.github.laxika.magicalvibes.cards.u.UnrulyMob;
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

@CardUsed({HostileHostel.class, CreepingInn.class, UnrulyMob.class, Island.class})
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
        addCreatureReady(player1, new UnrulyMob());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, hostel), 1, null, null);
        harness.passBothPriorities();

        assertThat(hostel.getCounterCount(CounterType.SOUL)).isOne();
        assertThat(hostel.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Unruly Mob");
    }

    @Test
    @DisplayName("The third soul counter transforms the land and untaps it")
    void thirdSoulCounterTransformsAndUntaps() {
        Permanent hostel = addReadyHostel(player1);
        hostel.setCounterCount(CounterType.SOUL, 2);
        addCreatureReady(player1, new UnrulyMob());
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
        addCreatureReady(player1, new UnrulyMob());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, hostel), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creeping Inn exiles a creature and drains each opponent by the number exiled with it")
    void attackTriggerUsesCreatureCardsExiledWithIt() {
        Permanent inn = addTransformedInn(player1);
        Card creature = new UnrulyMob();
        Card nonCreature = new Island();
        harness.setGraveyard(player1, List.of(creature, nonCreature));
        harness.setLife(player1, 20);
        harness.setLife(player2, 23);

        attack(inn);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        exileAndResolveDrain(creature);

        assertThat(gd.getLife(player2.getId())).isEqualTo(22);
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getCardsExiledByPermanent(inn.getId())).extracting(Card::getName)
                .containsExactly("Unruly Mob");
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

    @Test
    void drainResolvesTogetherWithExileWithoutAnotherPriorityRound() {
        Permanent inn = addTransformedInn(player1);
        Card creature = new UnrulyMob();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        attack(inn);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handleMayAbilityChosen(player1, true);
            harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

            harness.assertLife(player1, 21);
            harness.assertLife(player2, 19);
            assertThat(gd.stack).isEmpty();
        });
    }

    @Test
    void decliningExileDoesNotDrain() {
        Permanent inn = addTransformedInn(player1);
        Card creature = new UnrulyMob();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        attack(inn);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handleMayAbilityChosen(player1, false);
            resolveAllTriggers();
        });

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.getCardsExiledByPermanent(inn.getId())).isEmpty();
    }

    @Test
    void noCreatureInGraveyardDoesNotDrain() {
        Permanent inn = addTransformedInn(player1);
        Card land = new Island();
        harness.setGraveyard(player1, List.of(land));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            attack(inn);
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
                harness.handleMayAbilityChosen(player1, true);
            }
            resolveAllTriggers();
        });

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void phasingPreservesExiledCardsAndSubsequentAttackDrainsTwo() {
        Permanent inn = addTransformedInn(player1);
        Card first = new UnrulyMob();
        Card second = new UnrulyMob();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        attack(inn);
        exileAndResolveDrain(first);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.activateAbility(player1, indexOf(player1, inn), 0, null, null);
            harness.passBothPriorities();
        });

        assertThat(inn.isAttacking()).isFalse();
        harness.performUntapStep(player2);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(inn);
        harness.performUntapStep(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(inn);
        assertThat(inn.isTransformed()).isTrue();
        assertThat(inn.isTapped()).isFalse();

        attack(inn);
        exileAndResolveDrain(second);

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
        assertThat(gd.getCardsExiledByPermanent(inn.getId())).containsExactly(first, second);
    }

    @Test
    void soulAbilityCannotBeActivatedWithoutACreature() {
        Permanent hostel = addReadyHostel(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, hostel), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(hostel.isTapped()).isFalse();
        assertThat(hostel.getCounterCount(CounterType.SOUL)).isZero();
    }

    private Permanent addReadyHostel(Player player) {
        return addCreatureReady(player, new HostileHostel());
    }

    private Permanent addTransformedInn(Player player) {
        HostileHostel card = new HostileHostel();
        Permanent inn = addCreatureReady(player, card);
        inn.setCard(card.getBackFaceCard());
        inn.setTransformed(true);
        return inn;
    }

    private void attack(Permanent inn) {
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(indexOf(player1, inn)));
            harness.passBothPriorities();
        });
    }

    private void exileAndResolveDrain(Card creature) {
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handleMayAbilityChosen(player1, true);
            harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
            resolveAllTriggers();
        });
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
