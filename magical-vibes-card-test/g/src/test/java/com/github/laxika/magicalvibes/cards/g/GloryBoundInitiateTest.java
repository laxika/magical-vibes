package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BattlefieldScavenger;
import com.github.laxika.magicalvibes.cards.l.LayClaim;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({GloryBoundInitiate.class})
class GloryBoundInitiateTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadyInitiate(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting gives +1/+3 and lifelink until end of turn")
    void exertBoostsAndGrantsLifelink() {
        Permanent initiate = addReadyInitiate(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Exerting keeps the creature tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent initiate = addReadyInitiate(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(initiate.isTapped()).isTrue();
        assertThat(initiate.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert leaves base stats and grants no lifelink")
    void decliningExertDoesNothing() {
        Permanent initiate = addReadyInitiate(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isFalse();
        assertThat(initiate.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Exert is paid before opponents can respond to the bonus")
    void exertRestrictionIsPaidBeforeBonusResolves() {
        Permanent initiate = addReadyInitiate(player1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(initiate.getSkipUntapCount()).isPositive();
            assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isFalse();
        });
    }

    @Test
    @DisplayName("Exert skips only the next untap step")
    void exertedCreatureUntapsOnFollowingUntapStep() {
        Permanent initiate = addReadyInitiate(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(initiate.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(initiate.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(initiate.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The boost and lifelink expire at end of turn")
    void exertBonusExpiresAtEndOfTurn() {
        Permanent initiate = addReadyInitiate(player1);
        harness.setLibrary(player2, List.of(new GloryBoundInitiate()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, initiate, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @CardUsed({LayClaim.class})
    @DisplayName("An exerted creature can untap during its new controller's untap step")
    void exertRestrictionDoesNotFollowNewController() {
        Permanent initiate = addReadyInitiate(player1);

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
        harness.castEnchantment(player2, 0, initiate.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(initiate);
        harness.performUntapStep(player2);
        assertThat(initiate.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Exerted combat damage gains life through lifelink")
    void exertedCombatDamageGainsLife() {
        addReadyInitiate(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    @CardUsed({BattlefieldScavenger.class})
    @DisplayName("Exerting the Initiate triggers abilities that watch for exert")
    void exertTriggersBattlefieldScavenger() {
        addReadyInitiate(player1);
        harness.addToBattlefield(player1, new BattlefieldScavenger());
        harness.setHand(player1, List.of(new GloryBoundInitiate()));
        harness.setLibrary(player1, List.of(new GloryBoundInitiate()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private Permanent addReadyInitiate(Player player) {
        return addCreatureReady(player, new GloryBoundInitiate());
    }
}
