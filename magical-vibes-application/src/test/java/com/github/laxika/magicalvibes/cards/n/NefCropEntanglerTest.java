package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BattlefieldScavenger;
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

@CardUsed({NefCropEntangler.class, LayClaim.class, BattlefieldScavenger.class})
class NefCropEntanglerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadyEntangler(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting gives +1/+2 until end of turn")
    void exertBoosts() {
        Permanent entangler = addReadyEntangler(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, entangler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entangler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exerting keeps the creature tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent entangler = addReadyEntangler(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(entangler.isTapped()).isTrue();
        assertThat(entangler.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert leaves base stats and does not skip untap")
    void decliningExertDoesNothing() {
        Permanent entangler = addReadyEntangler(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, entangler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, entangler)).isEqualTo(1);
        assertThat(entangler.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Exert is paid before the bonus trigger resolves")
    void exertIsPaidBeforeBonusResolves() {
        Permanent entangler = addReadyEntangler(player1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, true);

            assertThat(entangler.getSkipUntapCount()).isPositive();
            assertThat(gqs.getEffectivePower(gd, entangler)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, entangler)).isEqualTo(1);
        });
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, entangler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, entangler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exert skips only the next untap step of the player who exerted it")
    void exertSkipsOnlyOneUntapStep() {
        Permanent entangler = addReadyEntangler(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(entangler.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(entangler.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(entangler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The exert bonus expires at end of turn")
    void exertBonusExpiresAtEndOfTurn() {
        Permanent entangler = addReadyEntangler(player1);
        harness.setLibrary(player2, List.of(new NefCropEntangler()));

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, entangler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, entangler)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exert does not prevent untapping during a new controller's untap step")
    void exertRestrictionDoesNotFollowNewController() {
        Permanent entangler = addReadyEntangler(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LayClaim()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.ensurePriority(player2);
        harness.castEnchantment(player2, 0, entangler.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(entangler);
        harness.performUntapStep(player2);
        assertThat(entangler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Exerting triggers Battlefield Scavenger even when it does not attack")
    void exertTriggersScavenger() {
        addReadyEntangler(player1);
        addCreatureReady(player1, new BattlefieldScavenger());
        harness.setHand(player1, List.of(new NefCropEntangler()));
        harness.setLibrary(player1, List.of(new NefCropEntangler()));

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
    }

    private Permanent addReadyEntangler(Player player) {
        return addCreatureReady(player, new NefCropEntangler());
    }
}
