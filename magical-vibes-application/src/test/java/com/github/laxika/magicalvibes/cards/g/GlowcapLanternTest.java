package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlowcapLantern.class, Forest.class, GrizzlyBears.class})
class GlowcapLanternTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping and attacking explores with a land on top")
    void equippedCreatureExploresLandIntoHand() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lantern = addLanternReady(player1);
        lantern.setAttachedTo(creature.getId());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Equipped creature explores a nonland and may put it into the graveyard")
    void equippedCreatureExploresNonland() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lantern = addLanternReady(player1);
        lantern.setAttachedTo(creature.getId());
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonland));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonland);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(nonland);
    }

    @Test
    @DisplayName("An unattached lantern does not grant explore")
    void unattachedLanternDoesNotTrigger() {
        addCreatureReady(player1, new GrizzlyBears());
        addLanternReady(player1);
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addLanternReady(Player player) {
        Permanent lantern = new Permanent(new GlowcapLantern());
        lantern.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(lantern);
        return lantern;
    }
}
