package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BonesplitterSliver;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SquallLine;
import com.github.laxika.magicalvibes.cards.s.SuddenDeath;
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

@CardUsed({PulmonicSliver.class, BonesplitterSliver.class, AshcoatBear.class, SuddenDeath.class, Forest.class,
        SquallLine.class})
class PulmonicSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Pulmonic Sliver gives flying to all Sliver creatures")
    void givesFlyingToSliverCreatures() {
        Permanent pulmonic = addCreatureReady(player1, new PulmonicSliver());
        Permanent sliver = addCreatureReady(player2, new BonesplitterSliver());
        Permanent bear = addCreatureReady(player2, new AshcoatBear());

        assertThat(gqs.hasKeyword(gd, pulmonic, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A Sliver controller may put a dying Sliver on top of its owner's library")
    void mayPutDyingSliverOnTopOfLibrary() {
        Card filler = new Forest();
        harness.setLibrary(player2, List.of(filler));
        addCreatureReady(player1, new PulmonicSliver());
        Permanent sliver = addCreatureReady(player2, new BonesplitterSliver());
        killWithSuddenDeath(sliver);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(sliver.getCard(), filler);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(sliver.getCard());
    }

    @Test
    @DisplayName("Declining Pulmonic Sliver's replacement puts the Sliver into its graveyard")
    void decliningReplacementLetsSliverDie() {
        addCreatureReady(player1, new PulmonicSliver());
        Permanent sliver = addCreatureReady(player1, new BonesplitterSliver());
        killWithSuddenDeath(sliver);

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
        Permanent sliver = addCreatureReady(player1, new BonesplitterSliver());

        harness.setHand(player2, List.of(new SquallLine()));
        harness.addMana(player2, ManaColor.GREEN, 5);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, 3, null);
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

    @Test
    @DisplayName("Pulmonic Sliver can replace its own death")
    void replacesItsOwnDeath() {
        Card filler = new Forest();
        harness.setLibrary(player1, List.of(filler));
        Permanent pulmonic = addCreatureReady(player1, new PulmonicSliver());

        killWithSuddenDeath(pulmonic);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(pulmonic);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(pulmonic.getCard(), filler);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(pulmonic.getCard());
    }

    @Test
    @DisplayName("A Sliver's controller chooses but its owner receives the card")
    void returnsStolenSliverToOwnersLibrary() {
        Card filler = new Forest();
        harness.setLibrary(player2, List.of(filler));
        addCreatureReady(player1, new PulmonicSliver());
        Card stolenCard = new BonesplitterSliver();
        stolenCard.setOwnerId(player2.getId());
        Permanent stolen = addCreatureReady(player1, stolenCard);

        killWithSuddenDeath(stolen);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(stolenCard, filler);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(stolenCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(stolenCard);
    }

    @Test
    @DisplayName("Non-Slivers go to the graveyard without a replacement choice")
    void doesNotReplaceNonSliverDeath() {
        addCreatureReady(player1, new PulmonicSliver());
        Permanent bear = addCreatureReady(player1, new AshcoatBear());

        killWithSuddenDeath(bear);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bear.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bear.getCard());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
    private void killWithSuddenDeath(Permanent target) {
        harness.setHand(player2, List.of(new SuddenDeath()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, target.getId());
    }
}
