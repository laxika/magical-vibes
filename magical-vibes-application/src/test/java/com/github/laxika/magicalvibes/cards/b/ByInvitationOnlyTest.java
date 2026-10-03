package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ByInvitationOnly.class, GrizzlyBears.class, Mountain.class})
class ByInvitationOnlyTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing zero sacrifices no creatures")
    void choosingZeroSacrificesNothing() {
        Permanent ownCreature = addCreature(player1);
        Permanent opposingCreature = addCreature(player2);

        castByInvitationOnly();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "0");

        assertThat(isOnBattlefield(player1, ownCreature)).isTrue();
        assertThat(isOnBattlefield(player2, opposingCreature)).isTrue();
    }

    @Test
    @DisplayName("Choosing thirteen sacrifices all creatures but not lands")
    void choosingThirteenSacrificesAllCreatures() {
        Permanent ownCreature = addCreature(player1);
        Permanent opposingCreature = addCreature(player2);
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Mountain());

        castByInvitationOnly();
        harness.handleListChoice(player1, "13");

        assertThat(isOnBattlefield(player1, ownCreature)).isFalse();
        assertThat(isOnBattlefield(player2, opposingCreature)).isFalse();
        assertThat(isOnBattlefield(player1, ownLand)).isTrue();
    }

    @Test
    @DisplayName("Each player chooses before all chosen creatures are sacrificed")
    void choicesAreCollectedBeforeSacrifice() {
        Permanent ownFirst = addCreature(player1);
        Permanent ownSecond = addCreature(player1);
        Permanent opposingFirst = addCreature(player2);
        Permanent opposingSecond = addCreature(player2);

        castByInvitationOnly();
        harness.handleListChoice(player1, "1");

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(ownFirst.getId()));

        assertThat(isOnBattlefield(player1, ownFirst)).isTrue();
        assertThat(isOnBattlefield(player2, opposingFirst)).isTrue();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(opposingFirst.getId()));

        assertThat(isOnBattlefield(player1, ownFirst)).isFalse();
        assertThat(isOnBattlefield(player2, opposingFirst)).isFalse();
        assertThat(isOnBattlefield(player1, ownSecond)).isTrue();
        assertThat(isOnBattlefield(player2, opposingSecond)).isTrue();
    }

    @Test
    @DisplayName("A player with fewer creatures sacrifices all of them after the other player's choice")
    void unequalCreatureCountsStillSacrificeSimultaneously() {
        Permanent ownCreature = addCreature(player1);
        Permanent opposingFirst = addCreature(player2);
        Permanent opposingSecond = addCreature(player2);
        Permanent opposingThird = addCreature(player2);

        castByInvitationOnly();
        harness.handleListChoice(player1, "2");
        harness.handleMultiplePermanentsChosen(player1, List.of(ownCreature.getId()));

        assertThat(isOnBattlefield(player1, ownCreature)).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(opposingFirst.getId(), opposingThird.getId()));

        assertThat(isOnBattlefield(player1, ownCreature)).isFalse();
        assertThat(isOnBattlefield(player2, opposingFirst)).isFalse();
        assertThat(isOnBattlefield(player2, opposingThird)).isFalse();
        assertThat(isOnBattlefield(player2, opposingSecond)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCreature.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(opposingFirst.getCard(), opposingThird.getCard());
    }

    @Test
    @DisplayName("A player with no creatures does not prevent the other player's sacrifice")
    void emptyBattlefieldDoesNotPreventSacrifice() {
        Permanent opposingFirst = addCreature(player2);
        Permanent opposingSecond = addCreature(player2);

        castByInvitationOnly();
        harness.handleListChoice(player1, "1");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(opposingSecond.getId()));

        assertThat(isOnBattlefield(player2, opposingFirst)).isTrue();
        assertThat(isOnBattlefield(player2, opposingSecond)).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingSecond.getCard());
    }

    @Test
    @DisplayName("Numbers outside zero through thirteen are rejected without sacrificing creatures")
    void rejectsOutOfRangeNumbers() {
        Permanent ownCreature = addCreature(player1);

        castByInvitationOnly();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "-1"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleListChoice(player1, "14"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(isOnBattlefield(player1, ownCreature)).isTrue();

        harness.handleListChoice(player1, "0");

        assertThat(isOnBattlefield(player1, ownCreature)).isTrue();
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private void castByInvitationOnly() {
        harness.castFromHand(player1, new ByInvitationOnly(), "{3}{W}{W}");
        harness.passBothPriorities();
    }

    private boolean isOnBattlefield(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(candidate -> candidate.getId().equals(permanent.getId()));
    }
}
