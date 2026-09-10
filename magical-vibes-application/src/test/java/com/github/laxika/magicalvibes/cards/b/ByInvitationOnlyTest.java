package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private void castByInvitationOnly() {
        harness.setHand(player1, List.of(new ByInvitationOnly()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private boolean isOnBattlefield(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .anyMatch(candidate -> candidate.getId().equals(permanent.getId()));
    }
}
