package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JadedResponse.class, GrizzlyBears.class, HillGiant.class})
class JadedResponseTest extends BaseCardTest {

    @Test
    void countersSpellSharingColorWithControlledCreature() {
        harness.addToBattlefield(player2, new HillGiant());

        HillGiant target = new HillGiant();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.setHand(player2, List.of(new JadedResponse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(se -> se.getCard().getId().equals(target.getId()));
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    void doesNotCounterSpellWithoutSharedColor() {
        harness.addToBattlefield(player2, new HillGiant());

        GrizzlyBears target = new GrizzlyBears();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new JadedResponse()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Jaded Response");
    }
}
