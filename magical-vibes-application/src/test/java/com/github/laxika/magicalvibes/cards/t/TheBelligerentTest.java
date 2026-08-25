package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheBelligerent.class, AirElemental.class, Forest.class, Shock.class})
class TheBelligerentTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a Treasure and grants top-library play until end of turn")
    void attackingCreatesTreasureAndGrantsTopLibraryPlay() {
        Shock shock = new Shock();
        attackWithBelligerent(shock);

        assertThat(findPermanent(player1, "Treasure")).isNotNull();
        assertThat(gd.playersAllowedToPlayFromLibraryTopUntilEndOfTurn).contains(player1.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveFromLibraryTop(player1, player2.getId());

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The attack trigger permits playing a land from the top of the library")
    void attackTriggerPermitsPlayingLandFromTop() {
        Forest forest = new Forest();
        attackWithBelligerent(forest);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Top-library permission is private and expires at cleanup")
    void topLibraryPermissionIsPrivateAndExpires() {
        Shock shock = new Shock();
        attackWithBelligerent(shock);

        harness.clearMessages();
        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Shock"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("\"revealedLibraryTopCards\"")
                        && message.contains("Shock"));

        com.github.laxika.magicalvibes.testutil.GameTestEngineContext.get()
                .getBean(com.github.laxika.magicalvibes.service.turn.TurnCleanupService.class)
                .applyCleanupResets(gd);

        assertThat(gd.playersAllowedToPlayFromLibraryTopUntilEndOfTurn).doesNotContain(player1.getId());
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void attackWithBelligerent(com.github.laxika.magicalvibes.model.Card topCard) {
        Permanent belligerent = harness.addToBattlefieldAndReturn(player1, new TheBelligerent());
        belligerent.setSummoningSick(false);
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        crew.setSummoningSick(false);
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
    }
}
