package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RathiFiend;
import com.github.laxika.magicalvibes.cards.s.StrongholdGambit;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrongholdGambit.class, GrizzlyBears.class, SerraAngel.class, Forest.class, LightningBolt.class,
        RathiFiend.class})
class StrongholdGambitTest extends BaseCardTest {

    @Test
    void putsEachLowestManaValueCreatureOntoTheBattlefield() {
        harness.setHand(player1, List.of(new StrongholdGambit(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new SerraAngel()));
        castGambit();

        choose(player1, 0);
        choose(player2, 0);

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(countPermanents(player2, "Serra Angel")).isZero();
        assertThat(gd.playerHands.get(player2.getId())).singleElement().isInstanceOf(SerraAngel.class);
    }

    @Test
    void putsAllCreaturesTiedForLowestManaValueOntoTheBattlefield() {
        harness.setHand(player1, List.of(new StrongholdGambit(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        castGambit();

        choose(player1, 0);
        choose(player2, 0);

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    void leavesNoncreatureChoicesInTheirOwnersHands() {
        harness.setHand(player1, List.of(new StrongholdGambit(), new Forest()));
        harness.setHand(player2, List.of(new LightningBolt()));
        castGambit();

        choose(player1, 0);
        choose(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(gd.playerHands.get(player2.getId())).singleElement().isInstanceOf(LightningBolt.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void skipsChoiceForPlayerWithEmptyHand() {
        harness.setHand(player1, List.of(new StrongholdGambit(), new GrizzlyBears()));
        harness.setHand(player2, List.of());
        castGambit();

        choose(player1, 0);

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void putsWinningCreatureUnderItsOwnersControl() {
        GrizzlyBears creatureOwnedByPlayer2 = new GrizzlyBears();
        creatureOwnedByPlayer2.setOwnerId(player2.getId());
        harness.setHand(player1, List.of(new StrongholdGambit(), creatureOwnedByPlayer2));
        harness.setHand(player2, List.of(new LightningBolt()));
        castGambit();

        choose(player1, 0);
        choose(player2, 0);

        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    void triggersEntersTheBattlefieldAbilityOfWinningCreature() {
        harness.setHand(player1, List.of(new StrongholdGambit(), new RathiFiend()));
        harness.setHand(player2, List.of(new LightningBolt()));
        castGambit();

        choose(player1, 0);
        choose(player2, 0);

        assertThat(countPermanents(player1, "Rathi Fiend")).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void ignoresLowerManaValueNoncreatureWhenChoosingWinningCreature() {
        harness.setHand(player1, List.of(new StrongholdGambit(), new Forest()));
        harness.setHand(player2, List.of(new SerraAngel()));
        castGambit();

        choose(player1, 0);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        choose(player2, 0);

        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
    }

    @Test
    void onlyComparesChosenCreaturesAndAllowsChoosingLaterHandIndex() {
        harness.setHand(player1, List.of(new StrongholdGambit(), new GrizzlyBears(), new SerraAngel()));
        harness.setHand(player2, List.of(new RathiFiend()));
        castGambit();

        choose(player1, 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        choose(player2, 0);

        harness.assertOnBattlefield(player2, "Rathi Fiend");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .<Class<?>>extracting(Object::getClass).containsExactly(GrizzlyBears.class, SerraAngel.class);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    void skipsEmptyCasterHandAndStillPutsOpponentsCreatureOntoBattlefield() {
        harness.setHand(player1, List.of(new StrongholdGambit()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        castGambit();

        choose(player2, 0);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithoutPromptWhenBothHandsAreEmpty() {
        harness.setHand(player1, List.of(new StrongholdGambit()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Stronghold Gambit");
    }

    private void castGambit() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.StrongholdGambitCardChoice.class);
    }

    private void choose(com.github.laxika.magicalvibes.model.Player player, int cardIndex) {
        harness.handleCardChosen(player, cardIndex);
    }
}
