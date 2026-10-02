package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CloudkinSeer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HealerOfTheGlade;
import com.github.laxika.magicalvibes.cards.l.LeafkinDruid;
import com.github.laxika.magicalvibes.cards.s.SoulsFire;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AtemsisAllSeeing.class, Forest.class, HealerOfTheGlade.class,
        LeafkinDruid.class, CloudkinSeer.class, AirElemental.class})
class AtemsisAllSeeingTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability draws two cards and then discards one")
    void activatedAbilityDrawsThenDiscards() {
        Permanent atemsis = addReadyAtemsis();
        harness.setHand(player1, List.of(new AtemsisAllSeeing()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore - 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(atemsis.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May reveal six different mana values and make the damaged opponent lose")
    void revealsSixDifferentManaValuesAndMakesOpponentLose() {
        addReadyAtemsis();
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(
                new Forest(), new HealerOfTheGlade(), new LeafkinDruid(),
                new CloudkinSeer(), new AirElemental(), new AtemsisAllSeeing()));

        dealCombatDamageToPlayer2();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining the reveal does not make the damaged opponent lose")
    void decliningRevealDoesNotMakeOpponentLose() {
        addReadyAtemsis();
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(
                new Forest(), new HealerOfTheGlade(), new LeafkinDruid(),
                new CloudkinSeer(), new AirElemental(), new AtemsisAllSeeing()));

        dealCombatDamageToPlayer2();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Revealing fewer than six different mana values does not make the opponent lose")
    void fewerThanSixDifferentManaValuesDoesNotLose() {
        addReadyAtemsis();
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Forest(), new HealerOfTheGlade(), new LeafkinDruid()));

        dealCombatDamageToPlayer2();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("With an empty hand, a newly drawn card can be discarded")
    void drawsBeforeChoosingDiscardFromEmptyHand() {
        addReadyAtemsis();
        harness.setHand(player1, List.of());
        Forest firstDraw = new Forest();
        HealerOfTheGlade secondDraw = new HealerOfTheGlade();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondDraw);
    }

    @Test
    @DisplayName("Six cards with duplicate mana values do not satisfy the condition")
    void sixCardsWithOnlyFiveDifferentManaValuesDoNotLose() {
        addReadyAtemsis();
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new HealerOfTheGlade(),
                new LeafkinDruid(), new CloudkinSeer(), new AirElemental()));

        dealCombatDamageToPlayer2();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @CardUsed(SoulsFire.class)
    @DisplayName("Noncombat damage to an opponent can make that opponent lose")
    void noncombatDamageToOpponentTriggersReveal() {
        Permanent atemsis = addReadyAtemsis();
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new SoulsFire(), new Forest(), new HealerOfTheGlade(),
                new LeafkinDruid(), new CloudkinSeer(), new AirElemental(), new AtemsisAllSeeing()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, List.of(atemsis.getId(), player2.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @CardUsed(SoulsFire.class)
    @DisplayName("Damage to Atemsis's controller does not trigger the reveal ability")
    void damageToControllerDoesNotTriggerReveal() {
        Permanent atemsis = addReadyAtemsis();
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, List.of(atemsis.getId(), player1.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private Permanent addReadyAtemsis() {
        return addCreatureReady(player1, new AtemsisAllSeeing());
    }

    private void dealCombatDamageToPlayer2() {
        Permanent atemsis = gd.playerBattlefields.get(player1.getId()).getFirst();
        atemsis.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
