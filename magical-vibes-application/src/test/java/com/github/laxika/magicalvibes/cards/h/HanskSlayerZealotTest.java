package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HanskSlayerZealot.class, GrizzlyBears.class})
class HanskSlayerZealotTest extends BaseCardTest {

    @Test
    @DisplayName("At upkeep, target opponent creates three Walker Zombie tokens")
    void createsThreeWalkerTokensForTargetOpponent() {
        addReadyHansk();

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        List<Permanent> walkers = findPermanents(player2, "Walker");
        assertThat(walkers).hasSize(3);
        assertThat(walkers).allMatch(walker -> walker.getCard().isToken()
                && walker.getCard().getPower() == 2
                && walker.getCard().getToughness() == 2
                && walker.getCard().getSubtypes().contains(CardSubtype.ZOMBIE));
    }

    @Test
    @DisplayName("Tap ability deals two damage to a target creature")
    void dealsTwoDamageToTargetCreature() {
        Permanent hansk = addReadyHansk();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hansk), null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Draws a card when an opponent's Zombie dies")
    void drawsWhenOpponentsZombieDies() {
        Permanent hansk = addReadyHansk();
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        Permanent walker = findPermanents(player2, "Walker").getFirst();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hansk), null, walker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
    }

    private Permanent addReadyHansk() {
        Permanent hansk = harness.addToBattlefieldAndReturn(player1, new HanskSlayerZealot());
        hansk.setSummoningSick(false);
        return hansk;
    }
}
