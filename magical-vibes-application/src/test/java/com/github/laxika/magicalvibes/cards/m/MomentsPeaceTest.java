package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MomentsPeace.class, DuskImp.class})
class MomentsPeaceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Moment's Peace prevents all combat damage this turn")
    void preventsAllCombatDamage() {
        harness.setHand(player1, List.of(new MomentsPeace()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(harness.getGameData().preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("Casting Moment's Peace prevents combat damage to players and creatures")
    void preventsCombatDamageToPlayersAndCreatures() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MomentsPeace()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        var blockedAttacker = addCreatureReady(player2, new DuskImp());
        var unblockedAttacker = addCreatureReady(player2, new DuskImp());
        var blocker = addCreatureReady(player1, new DuskImp());
        declareAttackers(player2, List.of(0, 1));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(blockedAttacker.getMarkedDamage()).isZero();
        assertThat(unblockedAttacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Moment's Peace stops preventing combat damage at the end of the turn")
    void combatDamagePreventionEndsAtEndOfTurn() {
        harness.setHand(player1, List.of(new MomentsPeace()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.preventAllCombatDamage).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isFalse();
    }

    @Test
    @DisplayName("Flashback prevents combat damage and exiles Moment's Peace")
    void flashbackPreventsCombatDamageAndExiles() {
        MomentsPeace card = new MomentsPeace();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, null);

        GameData gd = harness.getGameData();
        assertThat(gd.preventAllCombatDamage).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(graveyardCard -> graveyardCard.getId().equals(card.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(exiledCard -> exiledCard.getId().equals(card.getId()));
    }
}
