package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BallyrushBanneret;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StonybrookSchoolmaster.class, BallyrushBanneret.class})
class StonybrookSchoolmasterTest extends BaseCardTest {

    // "Whenever this creature becomes tapped, you may create a 1/1 blue Merfolk Wizard creature token."

    @Test
    @DisplayName("Tapping it and accepting creates a Merfolk Wizard token")
    void tappingAcceptCreatesToken() {
        Permanent schoolmaster = harness.addToBattlefieldAndReturn(player1, new StonybrookSchoolmaster());

        tap(schoolmaster);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(tokenCount()).isEqualTo(1);
        Permanent token = findPermanent(player1, "Merfolk Wizard");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColors()).containsExactly(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.MERFOLK, CardSubtype.WIZARD);
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Declining the trigger creates no token")
    void decliningCreatesNoToken() {
        Permanent schoolmaster = harness.addToBattlefieldAndReturn(player1, new StonybrookSchoolmaster());

        tap(schoolmaster);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(tokenCount()).isZero();
    }

    @Test
    @DisplayName("Tapping another creature you control does not trigger")
    void tappingOtherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new StonybrookSchoolmaster());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BallyrushBanneret());

        tap(other);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping an opponent's Schoolmaster creates a token for that opponent")
    void opponentSchoolmasterCreatesTokenForItsController() {
        Permanent opponentSchoolmaster = harness.addToBattlefieldAndReturn(player2, new StonybrookSchoolmaster());

        tap(opponentSchoolmaster);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(tokenCount(player1)).isZero();
        assertThat(tokenCount(player2)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking triggers token creation, but the token does not join combat")
    void attackingCreatesNonattackingToken() {
        Permanent schoolmaster = addCreatureReady(player1, new StonybrookSchoolmaster());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(schoolmaster.isTapped()).isTrue();
        assertThat(tokenCount()).isEqualTo(1);
        Permanent token = findPermanent(player1, "Merfolk Wizard");
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Each untap and tap in the same turn can create another token")
    void repeatedTapsCreateSeparateTokens() {
        Permanent schoolmaster = harness.addToBattlefieldAndReturn(player1, new StonybrookSchoolmaster());

        tap(schoolmaster);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        schoolmaster.untap();
        tap(schoolmaster);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(tokenCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Only the Schoolmaster that becomes tapped triggers")
    void anotherSchoolmasterDoesNotDuplicateTrigger() {
        Permanent schoolmaster = harness.addToBattlefieldAndReturn(player1, new StonybrookSchoolmaster());
        harness.addToBattlefield(player1, new StonybrookSchoolmaster());

        tap(schoolmaster);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(tokenCount()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tap trigger still creates its token after Schoolmaster leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent schoolmaster = harness.addToBattlefieldAndReturn(player1, new StonybrookSchoolmaster());

        tap(schoolmaster);
        gd.playerBattlefields.get(player1.getId()).remove(schoolmaster);
        gd.playerGraveyards.get(player1.getId()).add(schoolmaster.getCard());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(tokenCount()).isEqualTo(1);
        assertThat(tokenCount(player2)).isZero();
    }

    private long tokenCount() {
        return tokenCount(player1);
    }

    private long tokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && "Merfolk Wizard".equals(p.getCard().getName()))
                .count();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
