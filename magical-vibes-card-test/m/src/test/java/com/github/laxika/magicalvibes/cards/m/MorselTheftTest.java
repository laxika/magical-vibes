package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MorselTheft.class, ElvishWarrior.class, PricklyBoggart.class})
class MorselTheftTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    @Test
    @DisplayName("Normal cast: target player loses 3 life, controller gains 3 life, no draw")
    void normalCastDrainsNoDraw() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new MorselTheft()));
        harness.setLibrary(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2); // normal {2}{B}{B}
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(STARTING_LIFE - 3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 3);
        // No prowl — no draw, library untouched.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Prowl cast: drain resolves and the caster draws a card")
    void prowlCastDrainsAndDraws() {
        setupProwl();

        harness.setHand(player1, List.of(new MorselTheft()));
        harness.setLibrary(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1); // prowl {1}{B}
        harness.castWithProwl(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(STARTING_LIFE - 3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE + 3);
        // Prowl cost paid — draw a card.
        harness.assertInHand(player1, "Elvish Warrior");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Targeting yourself still applies the drain to the same player")
    void canTargetController() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new MorselTheft()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(STARTING_LIFE);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(STARTING_LIFE);
    }

    @Test
    @DisplayName("Prowl cost is unavailable without combat damage from a Rogue this turn")
    void prowlUnavailableWithoutRogueDamage() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new MorselTheft()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prowl cost is unavailable after combat damage from a non-Rogue")
    void prowlUnavailableAfterNonRogueDamage() {
        setupProwl(CardSubtype.GOBLIN);

        harness.setHand(player1, List.of(new MorselTheft()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithProwl(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paying the normal cost with prowl available does not draw")
    void normalCostWithProwlAvailableDoesNotDraw() {
        setupProwl();
        harness.setHand(player1, List.of(new MorselTheft()));
        harness.setLibrary(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A caster at 2 life survives targeting themselves and draws for prowl")
    void selfTargetAtLowLifeFinishesResolvingBeforeLossCheck() {
        setupProwl();
        harness.setLife(player1, 2);
        harness.setHand(player1, List.of(new MorselTheft()));
        harness.setLibrary(player1, List.of(new ElvishWarrior()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithProwl(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 2);
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Elvish Warrior");
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Actual Rogue combat damage enables prowl after the Rogue leaves")
    void rogueCombatDamageEnablesProwlAfterSourceLeaves() {
        addCreatureReady(player1, new PricklyBoggart());
        harness.setHand(player1, List.of(new MorselTheft()));
        harness.setLibrary(player1, List.of(new ElvishWarrior()));

        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            declareAttackers(List.of(0));
            resolveCombat();
        });
        harness.assertLife(player2, 19);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithProwl(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 16);
        harness.assertInHand(player1, "Elvish Warrior");
    }
    private void setupProwl() {
        setupProwl(CardSubtype.ROGUE);
    }

    private void setupProwl(CardSubtype subtype) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(subtype);
    }
}
