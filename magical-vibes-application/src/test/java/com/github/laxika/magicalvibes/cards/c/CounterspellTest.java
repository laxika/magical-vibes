package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.c.Commandeer;
import com.github.laxika.magicalvibes.cards.h.Hoodwink;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JhovallRider;
import com.github.laxika.magicalvibes.cards.s.StingingBarrier;
import com.github.laxika.magicalvibes.cards.w.WreakHavoc;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Counterspell.class, JhovallRider.class, Hoodwink.class, Island.class,
        Commandeer.class, StingingBarrier.class, WreakHavoc.class})
class CounterspellTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a spell")
    void castingTargetsSpell() {
        JhovallRider rider = new JhovallRider();
        harness.castFromHand(player1, rider, "{4}{W}");

        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, rider.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry entry = gd.stack.getLast();
        assertThat(entry.getCard()).isSameAs(counterspell);
        assertThat(entry.getTargetId()).isEqualTo(rider.getId());
    }

    @Test
    @DisplayName("Resolving counters a creature spell")
    void countersCreatureSpell() {
        JhovallRider rider = new JhovallRider();
        harness.castFromHand(player1, rider, "{4}{W}");

        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, rider.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(rider.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(rider.getId()));
    }

    @Test
    @DisplayName("Resolving counters a non-creature spell")
    void countersNonCreatureSpell() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        Hoodwink hoodwink = new Hoodwink();
        harness.setHand(player1, List.of(hoodwink));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, island.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hoodwink.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(hoodwink.getId()));
        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getId().equals(hoodwink.getId()));
    }

    @Test
    @DisplayName("Puts a spell controlled by another player into its owner's graveyard")
    void putsControlledSpellIntoOwnersGraveyard() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        Hoodwink hoodwink = new Hoodwink();
        Counterspell counterspell = new Counterspell();
        harness.setHand(player1, List.of(hoodwink, counterspell));
        harness.addMana(player1, ManaColor.BLUE, 4);

        Commandeer commandeer = new Commandeer();
        harness.setHand(player2, List.of(commandeer, new Counterspell(), new Counterspell()));

        harness.castInstant(player1, 0, island.getId());
        harness.passPriority(player1);
        harness.castInstantWithAlternateExileFromHand(player2, 0, hoodwink.getId(), List.of(1, 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.castInstant(player1, 0, hoodwink.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(hoodwink.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(hoodwink.getId()));
    }

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetRemoved() {
        JhovallRider rider = new JhovallRider();
        harness.castFromHand(player1, rider, "{4}{W}");

        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, rider.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getId().equals(rider.getId()));

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(counterspell.getId()));
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent barrier = harness.addToBattlefieldAndReturn(player1, new StingingBarrier());
        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, barrier.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell on the stack");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(counterspell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an activated ability")
    void cannotTargetActivatedAbility() {
        Permanent barrier = addCreatureReady(player1, new StingingBarrier());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, player2.getId());

        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, barrier.getCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell on the stack");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(counterspell);
    }

    @Test
    @DisplayName("Does not counter a spell that cannot be countered")
    void doesNotCounterUncounterableSpell() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        WreakHavoc wreakHavoc = new WreakHavoc();
        harness.setHand(player1, List.of(wreakHavoc));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        Counterspell counterspell = new Counterspell();
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, island.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, wreakHavoc.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(island.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(island.getCard().getId()));
    }
}
