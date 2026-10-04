package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.w.WashAway;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GutterSkulker.class, GutterShortcut.class, GrizzlyBears.class, DressDown.class, WashAway.class})
class GutterSkulkerTest extends BaseCardTest {

    @Test
    @DisplayName("Gutter Skulker can't be blocked while attacking alone")
    void frontFaceCannotBeBlockedWhileAttackingAlone() {
        Permanent skulker = addCreatureReady(player1, new GutterSkulker());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        skulker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(skulker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Gutter Skulker can be blocked when it attacks with another creature")
    void frontFaceCanBeBlockedWhenNotAttackingAlone() {
        Permanent skulker = addCreatureReady(player1, new GutterSkulker());
        Permanent companion = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        skulker.setAttacking(true);
        companion.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(skulker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Disturb casts Gutter Shortcut transformed and attached to a creature")
    void disturbEntersTransformedAttached() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent shortcut = castShortcut(bears);

        assertThat(shortcut.isTransformed()).isTrue();
        assertThat(shortcut.getCard()).isInstanceOf(GutterShortcut.class);
        assertThat(shortcut.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Gutter Shortcut makes its enchanted creature unblockable while attacking alone")
    void backFaceMakesEnchantedCreatureUnblockableWhileAttackingAlone() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castShortcut(bears);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        bears.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(bears)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Gutter Shortcut is exiled instead of going to the graveyard")
    void backFaceIsExiledInsteadOfGoingToGraveyard() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent shortcut = castShortcut(bears);
        UUID cardId = shortcut.getOriginalCard().getId();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, shortcut));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(cardId);
    }

    @Test
    void backFaceAllowsBlockingWhenEnchantedCreatureAttacksWithCompanion() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent companion = addCreatureReady(player1, new GrizzlyBears());
        castShortcut(bears);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        bears.setAttacking(true);
        companion.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(bears))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void backFaceCanEnchantOpponentsCreatureAndPreventBlocking() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent shortcut = castShortcut(bears);
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        prepareDeclareBlockers(player2);

        assertThat(shortcut.getAttachedTo()).isEqualTo(bears.getId());
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player2.getId()).indexOf(bears)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void enchantedCreatureRemainsUnblockableAfterLosingAbilities() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castShortcut(bears);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.castFromHand(player2, new DressDown(), "{1}{U}");
        resolveAllTriggers();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        bears.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(bears)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void frontFaceCanBeBlockedAfterLosingItsOwnAbilities() {
        Permanent skulker = addCreatureReady(player1, new GutterSkulker());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.castFromHand(player2, new DressDown(), "{1}{U}");
        resolveAllTriggers();
        skulker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(skulker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void frontFaceGoesToGraveyardNormally() {
        Permanent skulker = addCreatureReady(player1, new GutterSkulker());
        UUID cardId = skulker.getCard().getId();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, skulker));

        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(card -> card.getId())).contains(cardId);
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).doesNotContain(cardId);
    }

    @Test
    void backFaceIsExiledWhenEnchantedCreatureLeavesBattlefield() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent shortcut = castShortcut(bears);
        UUID cardId = shortcut.getOriginalCard().getId();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(shortcut);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(card -> card.getId())).doesNotContain(cardId);
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(cardId);
    }

    @Test
    void disturbedSpellIsExiledIfItsTargetLeavesBeforeResolution() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        prepareShortcutCast(bears);
        UUID cardId = gd.stack.getLast().getCard().getId();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(card -> card.getId())).doesNotContain(cardId);
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(cardId);
    }

    @Test
    void disturbedSpellIsExiledWhenCountered() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        prepareShortcutCast(bears);
        UUID cardId = gd.stack.getLast().getCard().getId();
        harness.setHand(player2, List.of(new WashAway()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, cardId);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(cardId);
    }

    private void prepareShortcutCast(Permanent enchantedCreature) {
        prepareDisturb();
        harness.castFlashback(player1, 0, enchantedCreature.getId());
    }

    private void prepareDisturb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new GutterSkulker()));
        harness.addMana(player1, ManaColor.BLUE, 4);
    }

    private Permanent castShortcut(Permanent enchantedCreature) {
        prepareDisturb();
        harness.castAndResolveFlashback(player1, 0, enchantedCreature.getId());

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTransformed)
                .findFirst()
                .orElseThrow();
    }
}
