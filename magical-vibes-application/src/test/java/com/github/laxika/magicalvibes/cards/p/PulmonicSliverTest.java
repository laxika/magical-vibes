package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PulmonicSliver.class, BonescytheSliver.class, GrizzlyBears.class, Murder.class, Forest.class,
        WrathOfGod.class})
class PulmonicSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Pulmonic Sliver gives flying to all Sliver creatures")
    void givesFlyingToSliverCreatures() {
        addCreatureReady(player1, new PulmonicSliver());
        Permanent sliver = addCreatureReady(player2, new BonescytheSliver());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A Sliver controller may put a dying Sliver on top of its owner's library")
    void mayPutDyingSliverOnTopOfLibrary() {
        Card filler = new Forest();
        harness.setLibrary(player1, List.of(filler));
        addCreatureReady(player1, new PulmonicSliver());
        Permanent sliver = addCreatureReady(player1, new BonescytheSliver());
        destroyWithMurder(sliver);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sliver.getCard(), filler);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(sliver.getCard());
    }

    @Test
    @DisplayName("Declining Pulmonic Sliver's replacement puts the Sliver into its graveyard")
    void decliningReplacementLetsSliverDie() {
        addCreatureReady(player1, new PulmonicSliver());
        Permanent sliver = addCreatureReady(player1, new BonescytheSliver());
        destroyWithMurder(sliver);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sliver.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(sliver.getCard());
    }

    @Test
    @DisplayName("Simultaneously dying Slivers each receive their own replacement choice")
    void handlesSimultaneousSliverDeaths() {
        Card filler = new Forest();
        harness.setLibrary(player1, List.of(filler));
        Permanent pulmonic = addCreatureReady(player1, new PulmonicSliver());
        Permanent sliver = addCreatureReady(player1, new BonescytheSliver());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.getGameService().playCard(gd, player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).contains(filler, pulmonic.getCard(), sliver.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .doesNotContain(pulmonic.getCard(), sliver.getCard());
    }

    private void destroyWithMurder(Permanent target) {
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}
