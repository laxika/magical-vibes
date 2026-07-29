package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrismaticCircleTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a color as it enters sets chosenColor on the enchantment")
    void choosingColorOnEnter() {
        harness.setHand(player1, List.of(new PrismaticCircle()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(findPermanent(player1, "Prismatic Circle").getChosenColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("Only sources of the chosen color may be chosen for the prevention shield")
    void onlyChosenColorSourcesAreValid() {
        addCircle(player1, CardColor.RED);
        Permanent goblin = addReady(player2, new GoblinPiker());
        addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(goblin.getId()));
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen source and consumes the shield")
    void preventsNextCombatDamage() {
        harness.setLife(player1, 20);
        addCircle(player1, CardColor.RED);
        Permanent goblin = addReady(player2, new GoblinPiker());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("No source choice is offered when no permanent matches the chosen color")
    void noMatchingColorSource() {
        addCircle(player1, CardColor.BLUE);
        addReady(player2, new GoblinPiker());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("No permanents on the battlefield"));
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps the circle")
    void paysCumulativeUpkeep() {
        Permanent circle = harness.addToBattlefieldAndReturn(player1, new PrismaticCircle());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(circle.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(circle);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices the circle")
    void declineSacrifices() {
        Permanent circle = harness.addToBattlefieldAndReturn(player1, new PrismaticCircle());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(circle);
        harness.assertInGraveyard(player1, "Prismatic Circle");
    }

    private Permanent addCircle(Player player, CardColor chosen) {
        Permanent perm = addReady(player, new PrismaticCircle());
        perm.setChosenColor(chosen);
        return perm;
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
