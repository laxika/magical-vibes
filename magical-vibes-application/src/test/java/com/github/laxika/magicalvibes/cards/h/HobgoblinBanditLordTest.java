package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HobgoblinBanditLord.class, GoblinPiker.class, GrizzlyBears.class})
class HobgoblinBanditLordTest extends BaseCardTest {

    @Test
    void boostsOtherGoblinsYouControl() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        Permanent nonGoblin = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentGoblin = harness.addToBattlefieldAndReturn(player2, new GoblinPiker());

        int goblinPower = gqs.getEffectivePower(gd, goblin);
        int goblinToughness = gqs.getEffectiveToughness(gd, goblin);
        int nonGoblinPower = gqs.getEffectivePower(gd, nonGoblin);
        int opponentGoblinPower = gqs.getEffectivePower(gd, opponentGoblin);

        harness.addToBattlefield(player1, new HobgoblinBanditLord());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(goblinPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(goblinToughness + 1);
        assertThat(gqs.getEffectivePower(gd, nonGoblin)).isEqualTo(nonGoblinPower);
        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(opponentGoblinPower);
    }

    @Test
    void dealsDamageEqualToOwnGoblinsThatEnteredThisTurn() {
        List<Card> ownEntries = new ArrayList<>(List.of(
                new GoblinPiker(), new GoblinPiker(), new GrizzlyBears()));
        gd.permanentsEnteredBattlefieldThisTurn.put(player1.getId(), ownEntries);
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(), List.of(new GoblinPiker()));

        Permanent lord = addCreatureReady(player1, new HobgoblinBanditLord());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(lord.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
