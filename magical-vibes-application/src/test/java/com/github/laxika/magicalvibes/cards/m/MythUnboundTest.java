package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MythUnbound.class, GrizzlyBears.class})
class MythUnboundTest extends BaseCardTest {

    @Test
    void reducesTheCommandersSecondCastAndDrawsWhenItReturnsToTheCommandZone() {
        harness.addToBattlefield(player1, new MythUnbound());
        Card commander = addCommanderToCommandZone();
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        castCommander(commander, 1, 1);
        Permanent commanderPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(commander.getId()))
                .findFirst().orElseThrow();
        harness.getPermanentRemovalService().removePermanentToCommandZone(gd, commanderPermanent);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);

        castCommander(commander, 2, 1);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(commander.getId()));
    }

    @Test
    void doesNotDrawWhenANonCommanderEntersTheCommandZone() {
        harness.addToBattlefield(player1, new MythUnbound());
        Permanent nonCommander = addCreatureReady(player1, new GrizzlyBears());
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.getPermanentRemovalService().removePermanentToCommandZone(gd, nonCommander);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Card addCommanderToCommandZone() {
        Card commander = new GrizzlyBears();
        commander.setOwnerId(player1.getId());
        commander.freeze();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        return commander;
    }

    private void castCommander(Card commander, int colorlessMana, int greenMana) {
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.addMana(player1, ManaColor.GREEN, greenMana);
        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));
    }
}
