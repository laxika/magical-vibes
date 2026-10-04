package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EliteSkirmisher;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FelhideSpiritbinder.class, EliteSkirmisher.class, FatedInfatuation.class})
class FelhideSpiritbinderTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{R} creates a hasty enchantment token copy and exiles it at end step")
    void payingCreatesHastyEnchantmentTokenCopy() {
        Permanent spiritbinder = addTappedSpiritbinder();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EliteSkirmisher());

        advanceToUntapStep();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(target.getId()).doesNotContain(spiritbinder.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ENCHANTMENT);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(token.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP));
    }

    @Test
    @DisplayName("Declining the payment creates no token")
    void decliningPaymentCreatesNoToken() {
        addTappedSpiritbinder();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EliteSkirmisher());

        advanceToUntapStep();
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(targetChoice).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The token is exiled at the beginning of the next end step")
    void tokenIsExiledAtNextEndStep() {
        addTappedSpiritbinder();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EliteSkirmisher());

        advanceToUntapStep();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Copying the token preserves enchantment but does not copy granted haste or exile")
    void copyingTokenDoesNotCopyGrantedHasteOrExile() {
        Permanent token = createToken();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FatedInfatuation()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, token.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken() && permanent != token)
                .findFirst().orElseThrow();
        assertThat(copy.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, copy, Keyword.HASTE)).isFalse();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(copy).doesNotContain(token);
    }

    @Test
    @DisplayName("The delayed exile retains the inspired ability's controller and source")
    void delayedExileRetainsOriginalControllerAndSource() {
        Permanent token = createToken();
        Permanent spiritbinder = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof FelhideSpiritbinder)
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(token);
        gd.playerBattlefields.get(player2.getId()).add(token);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spiritbinder.getCard());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(token);
    }

    @Test
    @DisplayName("A target leaving before resolution prevents payment and token creation")
    void departedTargetPreventsPaymentAndCopy() {
        addTappedSpiritbinder();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EliteSkirmisher());
        advanceToUntapStep();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The inspired ability resolves after Spiritbinder leaves the battlefield")
    void inspiredAbilitySurvivesSourceLeaving() {
        Permanent spiritbinder = addTappedSpiritbinder();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EliteSkirmisher());
        advanceToUntapStep();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(spiritbinder);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    private Permanent createToken() {
        addTappedSpiritbinder();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EliteSkirmisher());
        advanceToUntapStep();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
    }

    private Permanent addTappedSpiritbinder() {
        Permanent spiritbinder = harness.addToBattlefieldAndReturn(player1, new FelhideSpiritbinder());
        spiritbinder.setSummoningSick(false);
        spiritbinder.tap();
        return spiritbinder;
    }

    private void advanceToUntapStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
    }
}
