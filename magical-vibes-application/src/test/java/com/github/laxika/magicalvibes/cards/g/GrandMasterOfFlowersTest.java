package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrandMasterOfFlowers.class, GrizzlyBears.class})
class GrandMasterOfFlowersTest extends BaseCardTest {

    @Test
    @DisplayName("At seven loyalty, Grand Master becomes a 7/7 Dragon God creature")
    void becomesDragonGodCreatureAtSevenLoyalty() {
        Permanent grandMaster = addReadyGrandMaster(player1, 6);

        assertThat(gqs.isCreature(gd, grandMaster)).isFalse();
        assertThat(gqs.isPlaneswalker(gd, grandMaster)).isTrue();

        grandMaster.setCounterCount(CounterType.LOYALTY, 7);

        assertThat(gqs.isCreature(gd, grandMaster)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, grandMaster)).isFalse();
        assertThat(gqs.getEffectivePower(gd, grandMaster)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, grandMaster)).isEqualTo(7);
        assertThat(gqs.effectiveCreatureSubtypes(gd, grandMaster))
                .contains(CardSubtype.DRAGON, CardSubtype.GOD);
        assertThat(gqs.hasKeyword(gd, grandMaster, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, grandMaster, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The first +1 locks a qualifying creature until the controller's next turn")
    void firstPlusOneLocksCreature() {
        addReadyGrandMaster(player1, 5);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLockedFromAttacking(gd, target.getId())).isTrue();
        assertThat(gqs.isLockedFromBlocking(gd, target.getId())).isTrue();
    }

    @Test
    @DisplayName("The second +1 finds Monk of the Open Hand in the library")
    void secondPlusOneFindsMonkInLibrary() {
        addReadyGrandMaster(player1, 5);
        Card monk = new Card();
        monk.setName("Monk of the Open Hand");
        harness.setLibrary(player1, List.of(monk));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(monk.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(monk);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(monk);
    }

    private Permanent addReadyGrandMaster(Player player, int loyalty) {
        Permanent permanent = new Permanent(new GrandMasterOfFlowers());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
