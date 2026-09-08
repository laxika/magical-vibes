package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FootNinjas;
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

@CardUsed({SplinterRadicalRat.class, FootNinjas.class, GrizzlyBears.class})
class SplinterRadicalRatTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles triggered abilities of Ninjas you control")
    void doublesOwnNinjaTriggeredAbility() {
        harness.addToBattlefield(player1, new SplinterRadicalRat());
        harness.setHand(player1, List.of(new FootNinjas()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(26);
    }

    @Test
    @DisplayName("Does not double an opponent's Ninja triggered ability")
    void doesNotDoubleOpponentsNinjaTriggeredAbility() {
        harness.addToBattlefield(player1, new SplinterRadicalRat());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FootNinjas()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Makes a target Ninja unblockable and rejects non-Ninjas")
    void makesTargetNinjaUnblockable() {
        addCreatureReady(player1, new SplinterRadicalRat());
        Permanent ninja = addCreatureReady(player1, new FootNinjas());
        Permanent nonNinja = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonNinja.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, null, ninja.getId());
        harness.passBothPriorities();

        assertThat(ninja.isCantBeBlocked()).isTrue();
    }
}
