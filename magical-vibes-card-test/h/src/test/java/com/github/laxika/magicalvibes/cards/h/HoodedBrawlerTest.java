package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.l.LayClaim;
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

@CardUsed({HoodedBrawler.class, LayClaim.class})
class HoodedBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadyBrawler(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting gives +2/+2 until end of turn")
    void exertBoosts() {
        Permanent brawler = addReadyBrawler(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exerting keeps the creature tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent brawler = addReadyBrawler(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(brawler.isTapped()).isTrue();
        assertThat(brawler.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert leaves base stats and does not skip untap")
    void decliningExertDoesNothing() {
        Permanent brawler = addReadyBrawler(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(2);
        assertThat(brawler.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Exert is paid before the bonus trigger resolves")
    void exertIsPaidBeforeBonusResolves() {
        Permanent brawler = addReadyBrawler(player1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(brawler.getSkipUntapCount()).isPositive();
            assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(2);
            assertThat(gd.stack).hasSize(1);
        });
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exert skips only the next untap step of the player who exerted it")
    void exertSkipsOnlyOneUntapStep() {
        Permanent brawler = addReadyBrawler(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(brawler.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(brawler.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(brawler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The exert bonus expires at end of turn")
    void exertBonusExpiresAtEndOfTurn() {
        Permanent brawler = addReadyBrawler(player1);
        harness.setLibrary(player2, List.of(new HoodedBrawler()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exert does not prevent untapping during a new controller's untap step")
    void exertRestrictionDoesNotFollowNewController() {
        Permanent brawler = addReadyBrawler(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LayClaim()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.ensurePriority(player2);
        harness.castEnchantment(player2, 0, brawler.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(brawler);
        harness.performUntapStep(player2);
        assertThat(brawler.isTapped()).isFalse();
    }

    private Permanent addReadyBrawler(Player player) {
        return addCreatureReady(player, new HoodedBrawler());
    }
}
