package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZombieAssassin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DesmondMiles.class, ZombieAssassin.class, GrizzlyBears.class})
class DesmondMilesTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each other controlled Assassin and Assassin card in the graveyard")
    void boostsForAssassinsOnBattlefieldAndInGraveyard() {
        Permanent desmond = addCreatureReady(player1, new DesmondMiles());
        addCreatureReady(player1, new ZombieAssassin());
        addCreatureReady(player2, new ZombieAssassin());
        harness.setGraveyard(player1, List.of(new ZombieAssassin(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, desmond)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, desmond)).isEqualTo(3);
    }

    @Test
    @DisplayName("Surveils the amount of combat damage dealt")
    void surveilsCombatDamageAmount() {
        Permanent desmond = addCreatureReady(player1, new DesmondMiles());
        addCreatureReady(player1, new ZombieAssassin());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        declareAttackers(List.of(0));
        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(first, second);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }
}
