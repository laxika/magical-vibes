package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OperaLoveSong.class, GrizzlyBears.class, Mountain.class})
class OperaLoveSongTest extends BaseCardTest {

    @Test
    @DisplayName("Exile mode exiles the top two cards and grants play permission until your next end step")
    void exileMode() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));

        cast(0, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(first.getId(), second.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).containsKeys(first.getId(), second.getId());
    }

    @Test
    @DisplayName("Boost mode gives one or two target creatures +2/+0 until end of turn")
    void boostMode() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(1, List.of(first.getId(), second.getId()));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost mode allows exactly one target creature")
    void singleTargetAllowed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(1, List.of(target.getId()));

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost mode requires at least one creature target")
    void requiresAtLeastOneTarget() {
        harness.setHand(player1, List.of(new OperaLoveSong()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boost mode rejects a noncreature target")
    void requiresCreatureTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new OperaLoveSong()));
        addMana();

        UUID targetId = target.getId();
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new OperaLoveSong()));
        addMana();
        harness.castModalInstant(player1, 0, mode, targetIds);
        harness.passBothPriorities();
    }

    @Test
    void exileModeWithOneCardExilesOnlyThatCard() {
        Card only = new Mountain();
        harness.setLibrary(player1, List.of(only));

        cast(0, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(only);
        assertThat(gd.exilePlayPermissions).containsEntry(only.getId(), player1.getId());
    }

    @Test
    void exileModeWithEmptyLibraryDoesNotRequireTargets() {
        harness.setLibrary(player1, List.of());

        cast(0, List.of());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Opera Love Song");
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    void exiledLandsCanBePlayedButDoNotGrantAnAdditionalLandPlay() {
        Card first = new Mountain();
        Card second = new Mountain();
        harness.setLibrary(player1, List.of(first, second));

        cast(0, List.of());
        harness.castFromExile(player1, first.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(first).contains(second);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exiledCreatureRequiresNormalManaCost() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));

        cast(0, List.of());

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
    }

    @Test
    void exilePermissionEndsWhenControllersNextEndStepBegins() {
        Card exiled = new OperaLoveSong();
        harness.setLibrary(player1, List.of(exiled));

        cast(0, List.of());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(exiled.getId());
    }

    @Test
    void permissionCreatedDuringEndStepLastsUntilFollowingControllersEndStep() {
        Card exiled = new OperaLoveSong();
        harness.setLibrary(player1, List.of(exiled, new Mountain(), new Mountain()));
        harness.forceStep(TurnStep.END_STEP);

        cast(0, List.of());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).containsEntry(exiled.getId(), player1.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(exiled.getId());
    }

    @Test
    void opponentsExtraTurnDoesNotExpirePermissionBeforeControllersNextEndStep() {
        Card exiled = new OperaLoveSong();
        harness.setLibrary(player1, List.of(exiled, new Mountain(), new Mountain()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gd.queueExtraTurnFirst(player2.getId(), false);

        cast(0, List.of());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).containsEntry(exiled.getId(), player1.getId());
    }

    @Test
    void boostModeRejectsMoreThanTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OperaLoveSong()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostModeStillBoostsRemainingLegalTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OperaLoveSong()));
        addMana();
        harness.castModalInstant(player1, 0, 1, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerGraveyards.get(player1.getId()).add(first.getCard());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void boostEndsDuringCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(1, List.of(target.getId()));
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
