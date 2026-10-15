package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StromkirkOccultist.class, RavensCrime.class, Mountain.class})
class StromkirkOccultistTest extends BaseCardTest {

    private Permanent addReadyOccultist() {
        return addCreatureReady(player1, new StromkirkOccultist());
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

    /** Force player1 to discard Stromkirk Occultist via Raven's Crime from player2. */
    private StromkirkOccultist discardViaRavensCrime() {
        StromkirkOccultist occultist = new StromkirkOccultist();
        harness.setHand(player1, List.of(occultist));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return occultist;
    }

    private void resolveCombatDamageTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        // One pass deals combat damage and resolves the trigger via auto-pass. A second pass can
        // advance through end-step cleanup and clear EOT exile-play permissions.
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Combat damage to a player exiles the top card with end-of-turn play permission")
    void combatDamageExilesTopWithPlayPermission() {
        addReadyOccultist().setAttacking(true);
        Card top = putSpellOnTop("Exiled Spell");
        harness.setLife(player2, 20);

        resolveCombatDamageTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(top.getId());
    }

    @Test
    @DisplayName("Play permission from combat damage expires at end of turn")
    void playPermissionExpiresAtEndOfTurn() {
        addReadyOccultist().setAttacking(true);
        Card top = putSpellOnTop("Exiled Spell");

        resolveCombatDamageTrigger();
        assertThat(gd.exilePlayPermissions).containsKey(top.getId());

        harness.inMutationScope(
                () -> GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(top.getId());
    }

    @Test
    @DisplayName("Discarding Stromkirk Occultist exiles it and offers madness cast")
    void discardTriggersMadness() {
        StromkirkOccultist occultist = discardViaRavensCrime();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(occultist.getId()));
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getLast().getDescription()).contains("madness");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining madness cast puts the card into the graveyard")
    void decliningMadnessGoesToGraveyard() {
        StromkirkOccultist occultist = discardViaRavensCrime();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(occultist.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(occultist.getId()));
    }

    @Test
    @DisplayName("Accepting madness cast pays {1}{R} and puts the creature on the battlefield")
    void acceptingMadnessCastsCreature() {
        StromkirkOccultist occultist = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(occultist.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The exiled creature can be cast for its normal mana cost")
    void castsExiledCreatureForNormalCost() {
        addReadyOccultist().setAttacking(true);
        StromkirkOccultist top = new StromkirkOccultist();
        harness.setLibrary(player1, List.of(top));
        resolveCombatDamageTrigger();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castFromExile(player1, top.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(top.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(top.getId()));
    }

    @Test
    @DisplayName("The combat trigger allows playing an exiled land")
    void playsExiledLand() {
        addReadyOccultist().setAttacking(true);
        Mountain top = new Mountain();
        harness.setLibrary(player1, List.of(top));
        resolveCombatDamageTrigger();

        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN,
                () -> harness.castFromExile(player1, top.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(top.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(top.getId()));
    }

    @Test
    @DisplayName("Combat damage with an empty library does not exile a card")
    void emptyLibraryDoesNothing() {
        addReadyOccultist().setAttacking(true);
        harness.setLibrary(player1, List.of());

        resolveCombatDamageTrigger();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exile permission does not grant free casting or the madness cost")
    void exilePermissionRequiresNormalCost() {
        addReadyOccultist().setAttacking(true);
        StromkirkOccultist top = new StromkirkOccultist();
        harness.setLibrary(player1, List.of(top));
        resolveCombatDamageTrigger();
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }
}
