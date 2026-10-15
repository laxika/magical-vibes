package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SatyrGrovedancer;
import com.github.laxika.magicalvibes.cards.m.ManaConfluence;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PropheticFlamespeaker.class, SatyrGrovedancer.class, ManaConfluence.class})
class PropheticFlamespeakerTest extends BaseCardTest {

    private Permanent addReadyFlamespeaker() {
        return addCreatureReady(player1, new PropheticFlamespeaker());
    }

    private Card putSpellOnTop(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{4}{R}{R}");
        card.setColor(CardColor.RED);
        gd.playerDecks.get(player1.getId()).addFirst(card);
        return card;
    }

    private void resolveCombatDamageTrigger() {
        resolveCombat();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Combat damage to a player exiles the top card with end-of-turn play permission")
    void combatDamageExilesTopWithPlayPermission() {
        addReadyFlamespeaker().setAttacking(true);
        Card top = putSpellOnTop("Exiled Spell");

        resolveCombatDamageTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(top.getId());
    }

    @Test
    @DisplayName("Play permission from combat damage expires at end of turn")
    void playPermissionExpiresAtEndOfTurn() {
        addReadyFlamespeaker().setAttacking(true);
        Card top = putSpellOnTop("Exiled Spell");

        resolveCombatDamageTrigger();
        assertThat(gd.exilePlayPermissions).containsKey(top.getId());

        harness.inMutationScope(
                () -> GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(top.getId());
    }

    @Test
    void doubleStrikeExilesOneCardForEachDamageStep() {
        Card first = new PropheticFlamespeaker();
        Card second = new PropheticFlamespeaker();
        Card remaining = new PropheticFlamespeaker();
        harness.setLibrary(player1, List.of(first, second, remaining));
        harness.setLife(player2, 20);
        addReadyFlamespeaker().setAttacking(true);

        resolveCombatDamageTrigger();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
    }

    @Test
    void trampleAfterKillingBlockerExilesOnlyOneCard() {
        Card top = new PropheticFlamespeaker();
        Card remaining = new PropheticFlamespeaker();
        harness.setLibrary(player1, List.of(top, remaining));
        harness.setLife(player2, 20);
        addReadyFlamespeaker();
        harness.addToBattlefield(player2, new SatyrGrovedancer());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombatDamageTrigger();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Satyr Grovedancer");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    void emptyLibraryDoesNotPreventCombatDamage() {
        harness.setLibrary(player1, List.of());
        harness.setLife(player2, 20);
        addReadyFlamespeaker().setAttacking(true);

        resolveCombatDamageTrigger();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exiledCreatureRequiresManaAndCanBeCastInMainPhase() {
        Card top = new PropheticFlamespeaker();
        harness.setLibrary(player1, List.of(top));
        addReadyFlamespeaker().setAttacking(true);
        resolveCombatDamageTrigger();

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castFromExile(player1, top.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(top.getId()));
    }

    @Test
    void permissionDoesNotAllowOpponentOrCombatCreatureCasting() {
        Card top = new PropheticFlamespeaker();
        harness.setLibrary(player1, List.of(top));
        addReadyFlamespeaker().setAttacking(true);
        resolveCombatDamageTrigger();
        harness.addMana(player2, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castFromExile(player2, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 3);
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    void exiledLandsUseTheNormalLandPlayLimit() {
        Card first = new ManaConfluence();
        Card second = new ManaConfluence();
        harness.setLibrary(player1, List.of(first, second));
        addReadyFlamespeaker().setAttacking(true);
        resolveCombatDamageTrigger();

        harness.castFromExile(player1, first.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()));
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    void unplayedCardRemainsExiledAfterPermissionExpires() {
        Card top = new PropheticFlamespeaker();
        harness.setLibrary(player1, List.of(top));
        addReadyFlamespeaker().setAttacking(true);
        resolveCombatDamageTrigger();
        harness.inMutationScope(
                () -> GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
    }
}
