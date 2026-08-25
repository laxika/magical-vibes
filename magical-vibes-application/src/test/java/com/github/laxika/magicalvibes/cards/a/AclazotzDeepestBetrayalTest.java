package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AclazotzDeepestBetrayal.class, Forest.class, GrizzlyBears.class, Murder.class})
class AclazotzDeepestBetrayalTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes an opponent discard a land and creates a flying Bat")
    void attackingDiscardedLandCreatesBat() {
        Forest discarded = new Forest();
        harness.setHand(player2, List.of(discarded));
        Permanent aclazotz = addAclazotzReady(player1);

        declareAttackers(List.of(indexOf(player1, aclazotz)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        List<Permanent> bats = findPermanents(player1, "Bat");
        assertThat(bats).hasSize(1);
        assertThat(gqs.hasKeyword(gd, bats.get(0), Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Attacking draws for an opponent who cannot discard")
    void attackingDrawsForOpponentWithEmptyHand() {
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawn));
        Permanent aclazotz = addAclazotzReady(player1);

        declareAttackers(List.of(indexOf(player1, aclazotz)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("The back face taps for black mana")
    void backFaceTapsForBlackMana() {
        Permanent temple = addTempleReady(player1);

        harness.activateAbility(player1, indexOf(player1, temple), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(temple.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The back face transforms when any player has one or fewer cards in hand")
    void backFaceTransformsWhenAnyPlayerHasSmallHandAtActivation() {
        Permanent temple = addTempleReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(player1, temple), 1, null, null);
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.passBothPriorities();

        assertThat(temple.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("The back face cannot transform when every player has more than one card")
    void backFaceCannotTransformWithLargeHands() {
        Permanent temple = addTempleReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, temple), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dying returns the card transformed and tapped")
    void dyingReturnsTransformedAndTapped() {
        Permanent aclazotz = addAclazotzReady(player1);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, aclazotz.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent returned = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(returned.isTransformed()).isTrue();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(aclazotz.getCard().getId()));
    }

    private Permanent addAclazotzReady(Player player) {
        Permanent permanent = new Permanent(new AclazotzDeepestBetrayal());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addTempleReady(Player player) {
        AclazotzDeepestBetrayal card = new AclazotzDeepestBetrayal();
        Permanent permanent = new Permanent(card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
