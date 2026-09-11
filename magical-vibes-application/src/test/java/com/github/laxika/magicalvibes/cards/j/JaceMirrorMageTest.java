package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JaceMirrorMage.class, GrizzlyBears.class, Island.class})
class JaceMirrorMageTest extends BaseCardTest {

    @Test
    void kickedEntryCreatesNonLegendaryTokenWithOneLoyalty() {
        harness.setHand(player1, List.of(new JaceMirrorMage()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(token.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void plusOneScriesTwo() {
        Permanent jace = addReadyJace(player1, 4);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Island()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void zeroAbilityDrawsAndRemovesLoyaltyEqualToDrawnManaValue() {
        Permanent jace = addReadyJace(player1, 5);
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    private Permanent addReadyJace(Player player, int loyalty) {
        Permanent jace = new Permanent(new JaceMirrorMage());
        jace.setCounterCount(CounterType.LOYALTY, loyalty);
        jace.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(jace);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return jace;
    }
}
