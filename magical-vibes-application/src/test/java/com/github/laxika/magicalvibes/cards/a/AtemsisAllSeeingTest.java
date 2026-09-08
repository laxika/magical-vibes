package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
                new Forest(), new LlanowarElves(), new GrizzlyBears(),
                new Divination(), new SerraAngel(), new CrawWurm()));

        dealCombatDamageToPlayer2();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Declining the reveal does not make the damaged opponent lose")
    void decliningRevealDoesNotMakeOpponentLose() {
        addReadyAtemsis();
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(
                new Forest(), new LlanowarElves(), new GrizzlyBears(),
                new Divination(), new SerraAngel(), new CrawWurm()));

        dealCombatDamageToPlayer2();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Revealing fewer than six different mana values does not make the opponent lose")
    void fewerThanSixDifferentManaValuesDoesNotLose() {
        addReadyAtemsis();
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Forest(), new LlanowarElves(), new GrizzlyBears()));

        dealCombatDamageToPlayer2();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private Permanent addReadyAtemsis() {
        Permanent atemsis = new Permanent(new AtemsisAllSeeing());
        atemsis.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(atemsis);
        return atemsis;
    }

    private void dealCombatDamageToPlayer2() {
        Permanent atemsis = gd.playerBattlefields.get(player1.getId()).getFirst();
        atemsis.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }
}
