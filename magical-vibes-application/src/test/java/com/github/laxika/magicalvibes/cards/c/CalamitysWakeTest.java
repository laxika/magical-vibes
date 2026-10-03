package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoulfireGrandMaster;
import com.github.laxika.magicalvibes.cards.u.UrzasRebuff;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CalamitysWake.class, GiantGrowth.class, GrizzlyBears.class, Shock.class,
        SoulfireGrandMaster.class, UrzasRebuff.class})
class CalamitysWakeTest extends BaseCardTest {

    private List<Integer> playableCards(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player, List.of(new GiantGrowth(), new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 3);
        harness.clearPriorityPassed();
        harness.ensurePriority(player);
        return harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(harness.getGameData(), player.getId());
    }

    private void castWake() {
        harness.setHand(player1, List.of(new CalamitysWake()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);
    }

    @Test
    @DisplayName("exiles all graveyards and itself")
    void exilesAllGraveyardsAndItself() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        castWake();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Shock", "Calamity's Wake");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("locks noncreature spells for every player while allowing creature spells")
    void locksNoncreatureSpellsForEveryPlayer() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castWake();

        assertThat(playableCards(player1)).doesNotContain(0).contains(1);
        assertThat(playableCards(player2)).doesNotContain(0).contains(1);
    }

    @Test
    @DisplayName("noncreature spell lock ends with the turn")
    void noncreatureSpellLockEndsWithTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castWake();
        assertThat(playableCards(player2)).doesNotContain(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(playableCards(player2)).contains(0);
    }

    @Test
    @DisplayName("a noncreature spell already on the stack still resolves and enters the graveyard")
    void spellAlreadyOnStackStillResolves() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new CalamitysWake()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Calamity's Wake");
    }

    @Test
    @DisplayName("countering Wake preserves graveyards and does not restrict casting")
    void counteredWakeDoesNotApplyItsEffects() {
        CalamitysWake wake = new CalamitysWake();
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(wake));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setHand(player2, List.of(new UrzasRebuff()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{0}, wake.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Calamity's Wake");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Urza's Rebuff");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(playableCards(player1)).contains(0, 1);
        assertThat(playableCards(player2)).contains(0, 1);
    }

    @Test
    @DisplayName("Wake exiles itself even after Soulfire Grand Master's return-to-hand ability")
    void selfExileIsNotReplacedBySoulfireGrandMaster() {
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        castWake();

        harness.assertNotInHand(player1, "Calamity's Wake");
        harness.assertNotInGraveyard(player1, "Calamity's Wake");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Calamity's Wake");
    }
}
