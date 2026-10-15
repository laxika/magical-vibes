package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FieryImpulse;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbbotOfKeralKeep.class, FieryImpulse.class, Mountain.class})
class AbbotOfKeralKeepTest extends BaseCardTest {

    private void castAbbot(Player player) {
        harness.castFromHand(player, new AbbotOfKeralKeep(), "{1}{R}");
        harness.passBothPriorities();
        // Resolve the enters-the-battlefield trigger put on the stack by the creature resolving.
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles the top card with end-of-turn play permission at normal costs")
    void etbExilesTopCardWithPlayPermission() {
        Card top = new FieryImpulse();
        Card below = new FieryImpulse();
        harness.setLibrary(player1, List.of(below, top));

        castAbbot(player1);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(below.getId()));
        assertThat(gd.exilePlayPermissions.get(below.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(below.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(below.getId());

        // Only the single top card is exiled.
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(top.getId());
    }

    @Test
    @DisplayName("Exiles nothing when the library is empty")
    void exilesNothingWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        castAbbot(player1);

        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("Play permission is cleared during end-of-turn cleanup")
    void permissionExpiresAtEndOfTurn() {
        Card top = new FieryImpulse();
        harness.setLibrary(player1, List.of(top));

        castAbbot(player1);
        assertThat(gd.exilePlayPermissions).containsKey(top.getId());

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
    }

    @Test
    void exiledCreatureRequiresNormalManaCost() {
        Card top = new AbbotOfKeralKeep();
        harness.setLibrary(player1, List.of(top));
        castAbbot(player1);
        gd.playerManaPools.get(player1.getId()).clear();

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, top.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(top.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void exiledLandCanBePlayedWithoutTriggeringProwess() {
        Card top = new Mountain();
        harness.setLibrary(player1, List.of(top));
        castAbbot(player1);

        harness.castFromExile(player1, top.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        var abbot = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, abbot)).isEqualTo(2);
    }

    @Test
    void exiledLandStillRequiresAnUnusedLandPlay() {
        Card top = new Mountain();
        harness.setLibrary(player1, List.of(top));
        castAbbot(player1);
        harness.setHand(player1, List.of(new Mountain()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    void exiledLandCannotBePlayedOutsideAMainPhase() {
        Card top = new Mountain();
        harness.setLibrary(player1, List.of(top));
        castAbbot(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    void noncreatureSpellFromExileTriggersProwessBeforeResolving() {
        Card top = new FieryImpulse();
        harness.setLibrary(player1, List.of(top));
        castAbbot(player1);
        var abbot = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromExile(player1, top.getId(), abbot.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, abbot)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, abbot)).isEqualTo(2);
    }

    @Test
    void creatureSpellDoesNotTriggerProwess() {
        var abbot = harness.addToBattlefieldAndReturn(player1, new AbbotOfKeralKeep());
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new AbbotOfKeralKeep(), "{1}{R}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, abbot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, abbot)).isEqualTo(1);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTriggerProwess() {
        var abbot = harness.addToBattlefieldAndReturn(player1, new AbbotOfKeralKeep());
        harness.setHand(player2, List.of(new FieryImpulse()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, abbot.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, abbot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, abbot)).isEqualTo(1);
    }

    @Test
    void unplayedCardRemainsExiledAndCannotBeCastAfterCleanup() {
        Card top = new AbbotOfKeralKeep();
        harness.setLibrary(player1, List.of(top));
        castAbbot(player1);
        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }
}
