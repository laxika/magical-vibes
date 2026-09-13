package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrenzoDungeonWarden.class, GrizzlyBears.class, LowlandGiant.class, Shock.class})
class GrenzoDungeonWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters")
    void entersWithXPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new GrenzoDungeonWarden()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0, 3);
        harness.passBothPriorities();

        Permanent grenzo = findPermanent(player1, "Grenzo, Dungeon Warden");
        assertThat(grenzo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Puts the bottom creature card onto the battlefield when its power is low enough")
    void returnsBottomCreatureWithinGrenzoPower() {
        Permanent grenzo = addReadyGrenzo(player1, 2);
        Card topCard = new LowlandGiant();
        Card bottomCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareTurn();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bottomCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bottomCard);
        assertThat(grenzo.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Leaves a noncreature bottom card in the graveyard")
    void doesNotReturnNoncreatureBottomCard() {
        addReadyGrenzo(player1, 0);
        Card bottomCard = new Shock();
        harness.setLibrary(player1, List.of(bottomCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareTurn();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bottomCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bottomCard.getId()));
    }

    @Test
    @DisplayName("Leaves a creature with too much power in the graveyard")
    void doesNotReturnCreatureAboveGrenzoPower() {
        addReadyGrenzo(player1, 0);
        Card bottomCard = new LowlandGiant();
        harness.setLibrary(player1, List.of(bottomCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareTurn();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bottomCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bottomCard.getId()));
    }

    private Permanent addReadyGrenzo(Player player, int counters) {
        Permanent grenzo = new Permanent(new GrenzoDungeonWarden());
        grenzo.setSummoningSick(false);
        grenzo.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        gd.playerBattlefields.get(player.getId()).add(grenzo);
        return grenzo;
    }

    private void prepareTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
