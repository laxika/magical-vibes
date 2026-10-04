package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.Threaten;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GwenStacy.class, GhostSpider.class, Forest.class, GrizzlyBears.class, Threaten.class, Unsummon.class})
class GwenStacyTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card on entering and keeps it playable after transforming")
    void exilesTopCardAndKeepsPermissionAfterTransforming() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GwenStacy()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        prepareMainPhase();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent gwen = findPermanent(player1, "Gwen Stacy");
        assertThat(gd.getCardsExiledByPermanent(gwen.getId())).contains(topCard);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gwen.isTransformed()).isTrue();

        harness.clearPriorityPassed();
        gs.playCardFromExile(gd, player1, topCard.getId(), null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gwen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts a counter on itself when its controller casts a spell from exile")
    void countersWhenControllerCastsSpellFromExile() {
        Permanent ghostSpider = addBackReady(player1);
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell, ghostSpider.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        prepareMainPhase();
        gs.playCardFromExile(gd, player1, spell.getId(), null, null);
        resolveAllTriggers();

        assertThat(ghostSpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Removes two counters to exile the top card for the turn")
    void removesCountersToExileTopCardForTheTurn() {
        Permanent ghostSpider = addBackReady(player1);
        ghostSpider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        prepareMainPhase();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ghostSpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    void stealingGwenDoesNotTransferHerExiledCardPermission() {
        Forest topCard = enterGwenWithForest();
        Permanent gwen = findPermanent(player1, "Gwen Stacy");
        stealGwen(gwen);

        assertThatThrownBy(() -> gs.playCardFromExile(gd, player2, topCard.getId(), null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("No permission");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    void returningControlDoesNotRestoreTheEndedPermission() {
        Forest topCard = enterGwenWithForest();
        Permanent gwen = findPermanent(player1, "Gwen Stacy");
        stealGwen(gwen);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gwen);
        prepareMainPhase();

        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, topCard.getId(), null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("No permission");
    }

    @Test
    void leavingBeforeTheEnterTriggerResolvesDoesNotGrantPermission() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GwenStacy(), new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        prepareMainPhase();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent gwen = findPermanent(player1, "Gwen Stacy");
        harness.castAndResolveInstant(player1, 0, gwen.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, topCard.getId(), null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("No permission");
    }

    @Test
    void activatedPermissionSurvivesGhostSpiderLeavingBeforeResolution() {
        Permanent ghostSpider = addBackReady(player1);
        ghostSpider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        prepareMainPhase();
        harness.activateAbility(player1, 0, null, null);
        assertThat(ghostSpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
        harness.castAndResolveInstant(player1, 0, ghostSpider.getId());
        resolveAllTriggers();
        harness.clearPriorityPassed();

        gs.playCardFromExile(gd, player1, topCard.getId(), null, null);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void activatedPermissionExpiresAtEndOfTurn() {
        Permanent ghostSpider = addBackReady(player1);
        ghostSpider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        prepareMainPhase();
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        prepareMainPhase();

        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, topCard.getId(), null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("No permission");
    }

    @Test
    void playingFromHandDoesNotTriggerGhostSpider() {
        Permanent ghostSpider = addBackReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(ghostSpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotActivateWithOnlyOneCounter() {
        Permanent ghostSpider = addBackReady(player1);
        ghostSpider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ghostSpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
    }

    @Test
    void canRemoveNonPlusOneCountersEvenWithAnEmptyLibrary() {
        Permanent ghostSpider = addBackReady(player1);
        ghostSpider.setCounterCount(CounterType.CHARGE, 2);
        harness.setLibrary(player1, List.of());
        prepareMainPhase();
        harness.activateAbility(player1, 0, null, null);
        assertThat(ghostSpider.getCounterCount(CounterType.CHARGE)).isZero();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void transformAbilityCannotBeActivatedDuringCombat() {
        addCreatureReady(player1, new GwenStacy());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Gwen Stacy").isTransformed()).isFalse();
    }

    private Forest enterGwenWithForest() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GwenStacy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return topCard;
    }

    private void stealGwen(Permanent gwen) {
        harness.setHand(player2, List.of(new Threaten()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, gwen.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(gwen);
        harness.clearPriorityPassed();
    }
    @Test
    void controllerChoosesWhichKindsOfCountersToRemove() {
        Permanent ghostSpider = addBackReady(player1);
        ghostSpider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        ghostSpider.setCounterCount(CounterType.STUN, 2);
        harness.setLibrary(player1, List.of(new Forest()));
        prepareMainPhase();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(ghostSpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ghostSpider.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    private Permanent addBackReady(Player player) {
        GwenStacy card = new GwenStacy();
        Permanent permanent = addCreatureReady(player, card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
