package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrasssTunnelGrinder.class, GrizzlyBears.class})
class BrasssTunnelGrinderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by discarding any number, then draws that many plus one")
    void entersWithDiscardAndDraw() {
        Card discarded = new GrizzlyBears();
        Card drawnFirst = new GrizzlyBears();
        Card drawnSecond = new GrizzlyBears();
        BrasssTunnelGrinder grinder = new BrasssTunnelGrinder();
        harness.setHand(player1, List.of(grinder, discarded));
        harness.setLibrary(player1, List.of(drawnFirst, drawnSecond));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);

        resolveUntilInteraction();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnFirst, drawnSecond);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Puts a bore counter at your end step after descending and transforms at three")
    void descendsAndTransformsAtThreeBoreCounters() {
        BrasssTunnelGrinder grinder = new BrasssTunnelGrinder();
        Permanent permanent = new Permanent(grinder);
        permanent.setSummoningSick(false);
        permanent.setCounterCount(CounterType.BORE, 2);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        gd.playersWhoDescendedThisTurn.add(player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(permanent.isTransformed()).isTrue();
        assertThat(permanent.getCounterCount(CounterType.BORE)).isZero();
    }

    @Test
    @DisplayName("Tecutlan discovers using the mana value of a permanent spell cast with its mana")
    void backFaceDiscoversForPermanentSpellManaValue() {
        Permanent rift = addTransformedGrinder();
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(discovered));
        harness.setHand(player1, List.of(new BrasssTunnelGrinder()));

        harness.activateAbility(player1, indexOf(player1, rift), 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
    }

    private Permanent addTransformedGrinder() {
        BrasssTunnelGrinder card = new BrasssTunnelGrinder();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void resolveUntilInteraction() {
        while (gd.interaction.activeInteraction() == null && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
