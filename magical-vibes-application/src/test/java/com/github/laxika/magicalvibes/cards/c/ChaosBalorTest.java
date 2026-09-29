package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanHellkite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChaosBalor.class, Forest.class, GrizzlyBears.class, ShivanHellkite.class})
class ChaosBalorTest extends BaseCardTest {

    private static final String DISCARD_MODE =
            "Target player discards all the cards in their hand, then seeks that many nonland cards.";
    private static final String DAMAGE_AND_TREASURE_MODE =
            "Chaos Balor deals 2 damage to target player and they create two Treasure tokens.";
    private static final String CREATURE_MODE =
            "Chaos Balor deals 2 damage to each creature target player controls. Those creatures perpetually get +2/+0.";

    @Test
    void discardAndDamageModesTargetDifferentPlayers() {
        Card discarded = new Forest();
        Card sought = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(new Forest(), sought));

        attackWithBalor();
        chooseModes(DISCARD_MODE, DAMAGE_AND_TREASURE_MODE);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(sought);
        harness.assertLife(player2, 14);
        assertThat(findPermanents(player2, "Treasure")).hasSize(2);
    }

    @Test
    void creatureModeDealsDamageAndPerpetuallyBoostsThoseCreatures() {
        Permanent creature = addCreatureReady(player2, new ShivanHellkite());

        attackWithBalor();
        chooseModes(DISCARD_MODE, CREATURE_MODE);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    void deathTriggerUsesTheSameTwoModeChoice() {
        ChaosBalor card = new ChaosBalor();
        card.setToughness(1);
        Permanent balor = addCreatureReady(player1, card);
        balor.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    private void chooseModes(String first, String second) {
        harness.handleListChoice(player1, first);
        harness.handleListChoice(player1, second);
    }

    private void attackWithBalor() {
        addCreatureReady(player1, new ChaosBalor());
        declareAttackers(List.of(0));
    }
}
