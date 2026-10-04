package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.p.PhantasmalImage;
import com.github.laxika.magicalvibes.cards.w.WastefulHarvest;
import com.github.laxika.magicalvibes.cards.w.WingCommando;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GixianPuppeteer.class, ArgothianSprite.class, GoringWarplow.class,
        WingCommando.class, WastefulHarvest.class, PhantasmalImage.class})
class GixianPuppeteerTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers on the second card drawn each turn")
    void triggersOnSecondCardDrawn() {
        harness.addToBattlefield(player1, new GixianPuppeteer());
        harness.setLibrary(player1, List.of(new ArgothianSprite(), new ArgothianSprite(), new ArgothianSprite()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        drawAndResolveTrigger(player1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        drawAndResolveTrigger(player1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        drawAndResolveTrigger(player1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Dies and returns a target creature card with mana value 3 or less")
    void diesReturnsTargetCheapCreature() {
        harness.setGraveyard(player1, List.of(new ArgothianSprite()));

        killInCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        UUID targetId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();
        harness.handleMultipleCardsChosen(player1, List.of(targetId));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Argothian Sprite");
        harness.assertInGraveyard(player1, "Gixian Puppeteer");
    }

    @Test
    @DisplayName("Does not put the death ability on the stack without an eligible target")
    void diesWithoutEligibleTarget() {
        harness.setGraveyard(player1, List.of(new GoringWarplow()));

        killInCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Goring Warplow");
    }

    @Test
    void triggersOnControllersSecondDrawDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new GixianPuppeteer());
        harness.setLibrary(player1, List.of(new ArgothianSprite(), new ArgothianSprite()));

        drawAndResolveTrigger(player1);
        drawAndResolveTrigger(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void opponentsSecondDrawDoesNotTrigger() {
        harness.addToBattlefield(player1, new GixianPuppeteer());
        harness.setLibrary(player2, List.of(new ArgothianSprite(), new ArgothianSprite()));

        drawAndResolveTrigger(player2);
        drawAndResolveTrigger(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void countsDrawsBeforePuppeteerEntered() {
        harness.setLibrary(player1, List.of(new ArgothianSprite(), new ArgothianSprite()));
        drawAndResolveTrigger(player1);
        harness.addToBattlefield(player1, new GixianPuppeteer());

        drawAndResolveTrigger(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void returnsCreatureAtManaValueThreeBoundary() {
        WingCommando target = new WingCommando();
        harness.setGraveyard(player1, List.of(target, new WastefulHarvest(), new GoringWarplow()));

        killInCombat();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Wing Commando");
        harness.assertNotInGraveyard(player1, "Wing Commando");
        harness.assertInGraveyard(player1, "Wasteful Harvest");
        harness.assertInGraveyard(player1, "Goring Warplow");
    }

    @Test
    void doesNotTargetNoncreatureOrOpponentsCreature() {
        harness.setGraveyard(player1, List.of(new WastefulHarvest()));
        harness.setGraveyard(player2, List.of(new ArgothianSprite()));

        killInCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Wasteful Harvest");
        harness.assertInGraveyard(player2, "Argothian Sprite");
    }

    @Test
    void deathAbilityDoesNotReturnTargetThatLeftGraveyard() {
        ArgothianSprite target = new ArgothianSprite();
        harness.setGraveyard(player1, List.of(target));
        killInCombat();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(target);
        gd.playerHands.get(player1.getId()).add(target);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Argothian Sprite");
        assertThat(gd.playerHands.get(player1.getId())).contains(target);
    }

    @Test
    @CardUsed({PhantasmalImage.class})
    void lowManaValueCopyCannotReturnItself() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new GixianPuppeteer());
        harness.castFromHand(player1, new PhantasmalImage(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());
        Permanent copy = gd.playerBattlefields.get(player1.getId()).getFirst();

        copy.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Phantasmal Image");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void killInCombat() {
        Permanent puppeteer = harness.addToBattlefieldAndReturn(player1, new GixianPuppeteer());
        puppeteer.setSummoningSick(false);
        puppeteer.setBlocking(true);
        puppeteer.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GoringWarplow());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        resolveCombat(player2);
        harness.passBothPriorities();
    }

    private void drawAndResolveTrigger(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        resolveAllTriggers();
    }
}
