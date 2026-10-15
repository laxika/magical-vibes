package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KnightOfDawnsLight;
import com.github.laxika.magicalvibes.cards.y.YavimayaSteelcrusher;
import com.github.laxika.magicalvibes.cards.z.RoostOfDrakes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrongholdArena.class, YavimayaSteelcrusher.class, KnightOfDawnsLight.class,
        SheoldredTheApocalypse.class, RoostOfDrakes.class})
class StrongholdArenaTest extends BaseCardTest {

    @Test
    @DisplayName("Green kicker gains 3 life when Stronghold Arena enters")
    void greenKickerGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new StrongholdArena()));
        addMana(ManaColor.BLACK, ManaColor.COLORLESS, ManaColor.GREEN);

        castArena(true, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("White kicker gains 3 life when Stronghold Arena enters")
    void whiteKickerGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new StrongholdArena()));
        addMana(ManaColor.BLACK, ManaColor.COLORLESS, ManaColor.WHITE);

        castArena(false, List.of("{W}"));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Both kicker payments gain 6 life when Stronghold Arena enters")
    void bothKickersGainLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new StrongholdArena()));
        addMana(ManaColor.BLACK, ManaColor.COLORLESS, ManaColor.GREEN, ManaColor.WHITE);

        castArena(true, List.of("{W}"));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Combat damage trigger resolves once for multiple creatures and loses the revealed mana value")
    void combatDamageTriggerIsBatched() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new StrongholdArena());

        Permanent firstAttacker = addCreatureReady(player1, new YavimayaSteelcrusher());
        Permanent secondAttacker = addCreatureReady(player1, new YavimayaSteelcrusher());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);

        Card topCard = new YavimayaSteelcrusher();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(topCard.getId()));
    }

    @Test
    void unkickedArenaGainsNoLife() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new StrongholdArena(), "{1}{B}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    void bothKickersGainLifeInOneEvent() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new KnightOfDawnsLight());
        harness.setHand(player1, List.of(new StrongholdArena()));
        addMana(ManaColor.BLACK, ManaColor.COLORLESS, ManaColor.GREEN, ManaColor.WHITE);

        castArena(true, List.of("{W}"));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 27);
    }

    @Test
    void whiteOnlyKickerTriggersKickedSpellObserver() {
        harness.addToBattlefield(player1, new RoostOfDrakes());
        harness.setHand(player1, List.of(new StrongholdArena()));
        addMana(ManaColor.BLACK, ManaColor.COLORLESS, ManaColor.WHITE);

        castArena(false, List.of("{W}"));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Drake")).isEqualTo(1);
    }

    @Test
    void decliningRevealLeavesLibraryAndLifeUnchanged() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new StrongholdArena());
        addCreatureReady(player1, new YavimayaSteelcrusher()).setAttacking(true);
        Card topCard = new YavimayaSteelcrusher();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void acceptingRevealWithEmptyLibraryDoesNotLoseLifeOrDraw() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new StrongholdArena());
        addCreatureReady(player1, new YavimayaSteelcrusher()).setAttacking(true);

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void puttingRevealedCardIntoHandDoesNotTriggerSheoldred() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new StrongholdArena());
        harness.addToBattlefield(player1, new SheoldredTheApocalypse());
        addCreatureReady(player1, new YavimayaSteelcrusher()).setAttacking(true);
        Card topCard = new YavimayaSteelcrusher();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void firstStrikeAndNormalCombatDamageEachTriggerArena() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new StrongholdArena());
        addCreatureReady(player1, new KnightOfDawnsLight()).setAttacking(true);
        addCreatureReady(player1, new YavimayaSteelcrusher()).setAttacking(true);
        Card firstCard = new YavimayaSteelcrusher();
        Card secondCard = new StrongholdArena();
        harness.setLibrary(player1, List.of(firstCard, secondCard));

        resolveCombat();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
            resolveAllTriggers();
        }
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
    }

    private void addMana(ManaColor... colors) {
        for (ManaColor color : colors) {
            harness.addMana(player1, color, 1);
        }
    }

    private void castArena(boolean kicked, List<String> repeatedAdditionalCosts) {
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, kicked, null, null, null, null,
                repeatedAdditionalCosts, false);
    }
}
