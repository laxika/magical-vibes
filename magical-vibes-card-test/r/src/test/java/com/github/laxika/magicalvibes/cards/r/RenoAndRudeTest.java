package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
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

@CardUsed({RenoAndRude.class, Forest.class, GrizzlyBears.class, Ornithopter.class})
class RenoAndRudeTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the damaged player's top card but does not grant permission before sacrificing")
    void declineSacrificeLeavesCardExiledWithoutPermission() {
        Permanent reno = addAttackingReno();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard, new Forest()));

        resolveCombatAndTrigger();

        ExiledCardEntry exiled = gd.findExiledCard(topCard.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.sourcePermanentId()).isEqualTo(reno.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    @Test
    @DisplayName("Sacrificing another artifact grants end-of-turn play permission with any-color mana")
    void sacrificingArtifactGrantsPlayPermission() {
        addAttackingReno();
        Permanent artifact = addCreatureReady(player1, new Ornithopter());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).contains(topCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("The sacrificed branch lets the controller play an exiled land")
    void sacrificingCreatureLetsControllerPlayExiledLand() {
        addAttackingReno();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("Play permission is granted during the original trigger, without a separate trigger")
    void sacrificeGrantsPermissionWithoutAnotherPriorityRound() {
        addAttackingReno();
        Permanent artifact = addCreatureReady(player1, new Ornithopter());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(gd.currentStep,
                () -> harness.handlePermanentChosen(player1, artifact.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("A later sacrifice does not grant access to a card exiled by an earlier declined trigger")
    void laterSacrificeDoesNotUnlockEarlierExiledCard() {
        Permanent reno = addAttackingReno();
        Permanent artifact = addCreatureReady(player1, new Ornithopter());
        Card earlierCard = new GrizzlyBears();
        Card currentCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(earlierCard, currentCard, new Forest()));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        reno.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(earlierCard.getId())).isNotNull();
        assertThat(gd.findExiledCard(currentCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(currentCard.getId(), player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, earlierCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(earlierCard.getId());
    }

    @Test
    @DisplayName("Reno and Rude, lands, and opposing creatures cannot pay the sacrifice")
    void sacrificeChoiceExcludesSourceLandsAndOpponents() {
        Permanent reno = addAttackingReno();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponent = addCreatureReady(player2, new Ornithopter());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, reno.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("Unplayed exiled cards stay exiled after the permission expires at end of turn")
    void playPermissionExpiresAtEndOfTurn() {
        Permanent reno = addAttackingReno();
        Permanent artifact = addCreatureReady(player1, new Ornithopter());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard, new Forest()));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());

        reno.setAttacking(false);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).doesNotContain(topCard.getId());
    }

    @Test
    @DisplayName("Without another creature or artifact the card is still exiled but cannot be played")
    void noEligibleSacrificeDoesNotGrantPermission() {
        addAttackingReno();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Reno and Rude");
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    @DisplayName("Permission does not waive spell timing or the exiled card's mana cost")
    void exiledCreatureStillRequiresMainPhaseAndMana() {
        addAttackingReno();
        Permanent artifact = addCreatureReady(player1, new Ornithopter());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gd.playerManaPools.get(player1.getId()).clear();
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, topCard.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private Permanent addAttackingReno() {
        Permanent reno = addCreatureReady(player1, new RenoAndRude());
        reno.setAttacking(true);
        return reno;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
