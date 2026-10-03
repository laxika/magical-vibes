package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YouWillKnowTrueSuffering.class, CentaurCourser.class, GrizzlyBears.class})
class YouWillKnowTrueSufferingTest extends BaseCardTest {

    @Test
    void dealsDamageEqualToGreatestCommanderManaValueToOpponentsNoncommanders() {
        prepareCommander(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CentaurCourser());
        Permanent opposingCommander = addCommanderToBattlefield(player2);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CentaurCourser());

        resolveScheme();

        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingCommander.getMarkedDamage()).isZero();
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }

    @Test
    void commanderManaValueIsZeroWhenTheControllerHasNoCommander() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CentaurCourser());

        resolveScheme();

        assertThat(opposingCreature.getMarkedDamage()).isZero();
    }

    private Permanent addCommanderToBattlefield(com.github.laxika.magicalvibes.model.Player player) {
        Card commander = new GrizzlyBears();
        prepareCommander(player, commander);
        return harness.addToBattlefieldAndReturn(player, commander);
    }

    private void prepareCommander(com.github.laxika.magicalvibes.model.Player player, Card commander) {
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player.getId(), commander);
        gd.playerCommandZones.put(player.getId(), new ArrayList<>(List.of(commander)));
    }

    private void resolveScheme() {
        YouWillKnowTrueSuffering scheme = new YouWillKnowTrueSuffering();
        gd.stack.add(new com.github.laxika.magicalvibes.model.StackEntry(
                com.github.laxika.magicalvibes.model.StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(com.github.laxika.magicalvibes.model.EffectSlot.SPELL)));
        harness.passBothPriorities();
    }
}
