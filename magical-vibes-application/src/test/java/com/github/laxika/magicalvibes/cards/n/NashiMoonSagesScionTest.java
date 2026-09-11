package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NashiMoonSagesScion.class, GrizzlyBears.class})
class NashiMoonSagesScionTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the top card of each player's library")
    void combatDamageExilesEachLibraryTopCard() {
        Permanent nashi = addAttackingNashi();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveCombatAndTrigger();

        assertThat(gd.getCardsExiledByPermanent(nashi.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("One exiled card may be cast by paying life equal to its mana value")
    void castsOneExiledCardForLife() {
        Permanent nashi = addAttackingNashi();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveCombatAndTrigger();
        List<ExiledCardEntry> exiled = gd.exiledCards.stream()
                .filter(entry -> nashi.getId().equals(entry.sourcePermanentId()))
                .toList();
        ExiledCardEntry first = exiled.getFirst();
        ExiledCardEntry second = exiled.get(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, first.card().getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castFromExile(player1, second.card().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ninjutsu puts Nashi onto the battlefield tapped and attacking")
    void ninjutsu() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NashiMoonSagesScion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent nashi = findPermanent(player1, "Nashi, Moon Sage's Scion");
        assertThat(nashi.isTapped()).isTrue();
        assertThat(nashi.isAttacking()).isTrue();
        assertThat(nashi.getAttackTarget()).isEqualTo(player2.getId());
    }

    private Permanent addAttackingNashi() {
        Permanent nashi = addCreatureReady(player1, new NashiMoonSagesScion());
        nashi.setAttacking(true);
        return nashi;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
