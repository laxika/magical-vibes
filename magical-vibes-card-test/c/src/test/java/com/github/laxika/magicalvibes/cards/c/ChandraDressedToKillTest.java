package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BurrentonForgeTender;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({ChandraDressedToKill.class, FieryTemper.class, ThinkTwice.class, BurrentonForgeTender.class})
class ChandraDressedToKillTest extends BaseCardTest {

    @Test
    @DisplayName("+1 adds {R} and deals 1 damage to target player")
    void plusOneAddsManaAndDealsDamage() {
        Permanent chandra = addReadyChandra(player1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        int redBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(redBefore + 1);
    }

    @Test
    @DisplayName("+1 still adds {R} when no damage target is chosen")
    void plusOneAllowsDecliningDamageTarget() {
        Permanent chandra = addReadyChandra(player1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        int redBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(redBefore + 1);
    }

    @Test
    @DisplayName("+1 exile grants cast permission when the top card is red")
    void secondPlusOneExilesRedWithCastPermission() {
        Permanent chandra = addReadyChandra(player1);
        Card temper = new FieryTemper();
        harness.setLibrary(player1, List.of(temper));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(temper.getId()));
        assertThat(gd.exilePlayPermissions.get(temper.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(temper.getId());
    }

    @Test
    @DisplayName("+1 exile does not grant cast permission when the top card is not red")
    void secondPlusOneExilesNonRedWithoutCastPermission() {
        Permanent chandra = addReadyChandra(player1);
        Card thinkTwice = new ThinkTwice();
        harness.setLibrary(player1, List.of(thinkTwice));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(thinkTwice.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(thinkTwice.getId());
    }

    @Test
    @DisplayName("−7 exiles five cards, grants cast permission for red ones, and creates emblem")
    void minusSevenExilesFiveAndCreatesEmblem() {
        Permanent chandra = addReadyChandra(player1);
        chandra.setCounterCount(CounterType.LOYALTY, 7);

        Card red1 = new FieryTemper();
        Card blue = new ThinkTwice();
        Card red2 = new FieryTemper();
        Card nonRed = new ThinkTwice();
        Card red3 = new FieryTemper();
        harness.setLibrary(player1, List.of(red1, blue, red2, nonRed, red3));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Chandra, Dressed to Kill");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(red1.getId(), blue.getId(), red2.getId(), nonRed.getId(), red3.getId());
        assertThat(gd.exilePlayPermissions.get(red1.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions.get(red2.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions.get(red3.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(blue.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(nonRed.getId());

        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Emblem deals mana spent to cast a red spell to any target")
    void emblemDealsManaSpentDamageOnRedSpell() {
        createEmblem();

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 3);
        // Target the controller so emblem damage to the opponent is unambiguous.
        harness.castInstant(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Emblem does not trigger on a non-red spell")
    void emblemIgnoresNonRedSpells() {
        createEmblem();

        harness.setHand(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.pendingInteractions).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate −7 with insufficient loyalty")
    void cannotActivateMinusSevenWithInsufficientLoyalty() {
        addReadyChandra(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    void firstPlusOneUsesTheStackEvenWithoutATarget() {
        addReadyChandra(player1);
        int redBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(redBefore);
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(redBefore + 1);
    }

    @Test
    void firstPlusOneCanDamageAPlaneswalker() {
        addReadyChandra(player1);
        Permanent opposingChandra = harness.addToBattlefieldAndReturn(player2, new ChandraDressedToKill());
        opposingChandra.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 0, null, opposingChandra.getId());
        harness.passBothPriorities();

        assertThat(opposingChandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void firstPlusOneAddsNoManaIfItsOnlyTargetBecomesIllegal() {
        addReadyChandra(player1);
        Permanent opposingChandra = harness.addToBattlefieldAndReturn(player2, new ChandraDressedToKill());
        opposingChandra.setCounterCount(CounterType.LOYALTY, 3);
        int redBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);
        harness.activateAbility(player1, 0, 0, null, opposingChandra.getId());
        gd.playerBattlefields.get(player2.getId()).remove(opposingChandra);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(redBefore);
    }

    @Test
    void secondPlusOneAllowsCastingTheExiledRedCardByPayingItsCost() {
        addReadyChandra(player1);
        Card temper = new FieryTemper();
        harness.setLibrary(player1, List.of(temper));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 3);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromExile(player1, temper.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(temper);
    }

    @Test
    void secondPlusOneDoesNotWaiveTheExiledCardsManaCost() {
        addReadyChandra(player1);
        Card temper = new FieryTemper();
        harness.setLibrary(player1, List.of(temper));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, temper.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(temper);
    }

    @Test
    void secondPlusOneResolvesWithAnEmptyLibrary() {
        Permanent chandra = addReadyChandra(player1);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void ultimateExilesTheRemainingCardsOfAShortLibrary() {
        Permanent chandra = addReadyChandra(player1);
        chandra.setCounterCount(CounterType.LOYALTY, 8);
        Card temper = new FieryTemper();
        Card thinkTwice = new ThinkTwice();
        harness.setLibrary(player1, List.of(temper, thinkTwice));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(temper, thinkTwice);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.emblems).hasSize(1);
    }

    @Test
    void ultimateCreatesAnEmblemEvenWithAnEmptyLibrary() {
        createEmblem();

        assertThat(gd.emblems).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Chandra, Dressed to Kill");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void emblemCanTargetAndDamageACreatureWithProtectionFromRed() {
        createEmblem();
        Permanent forgeTender = harness.addToBattlefieldAndReturn(player2, new BurrentonForgeTender());
        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player1.getId());
        harness.handlePermanentChosen(player1, forgeTender.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Burrenton Forge-Tender");
    }

    @Test
    void emblemDoesNotTriggerForAnOpponentsRedSpell() {
        createEmblem();
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    private Permanent addReadyChandra(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChandraDressedToKill());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void createEmblem() {
        Permanent chandra = addReadyChandra(player1);
        chandra.setCounterCount(CounterType.LOYALTY, 7);
        harness.setLibrary(player1, List.of());
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
    }
}
