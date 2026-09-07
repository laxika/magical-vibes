package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GargoyleCastle;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObsessivePursuit.class, GargoyleCastle.class, GrizzlyBears.class})
class ObsessivePursuitTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by making its controller lose life and creating a Clue")
    void entersWithLifeLossAndClue() {
        harness.setHand(player1, List.of(new ObsessivePursuit()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Triggers on upkeep with another life loss and Clue")
    void triggersOnUpkeep() {
        harness.addToBattlefield(player1, new ObsessivePursuit());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Puts counters equal to permanents sacrificed and grants lifelink at three")
    void scalesWithSacrificedPermanents() {
        harness.addToBattlefield(player1, new ObsessivePursuit());
        Permanent firstCastle = addCastle();
        addCastle();
        addCastle();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 15);

        sacrificeCastle(firstCastle);
        sacrificeCastle(findPermanents(player1, "Gargoyle Castle").getFirst());
        sacrificeCastle(findPermanents(player1, "Gargoyle Castle").getFirst());
        resolveAllTriggers();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.LIFELINK)).isTrue();
    }

    private Permanent addCastle() {
        return addCreatureReady(player1, new GargoyleCastle());
    }

    private void sacrificeCastle(Permanent castle) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(castle), 1, null, null);
    }
}
