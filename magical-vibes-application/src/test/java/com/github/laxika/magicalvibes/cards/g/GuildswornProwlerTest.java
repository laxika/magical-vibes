package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuildswornProwler.class, GrizzlyBears.class, WrathOfGod.class})
class GuildswornProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies without blocking, it draws a card")
    void diesWithoutBlockingDrawsCard() {
        harness.addToBattlefield(player1, new GuildswornProwler());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int handSizeBeforeCast = gd.playerHands.get(player1.getId()).size();
        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Guildsworn Prowler"));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBeforeCast);
    }

    @Test
    @DisplayName("When it dies blocking, it does not draw a card")
    void diesBlockingDoesNotDrawCard() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GuildswornProwler());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Guildsworn Prowler"));
        harness.assertNotOnBattlefield(player2, "Guildsworn Prowler");
    }
}
