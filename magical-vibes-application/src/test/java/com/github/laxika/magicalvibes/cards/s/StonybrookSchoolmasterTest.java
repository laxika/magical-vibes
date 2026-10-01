package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BallyrushBanneret;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StonybrookSchoolmaster.class, BallyrushBanneret.class})
class StonybrookSchoolmasterTest extends BaseCardTest {

    // "Whenever this creature becomes tapped, you may create a 1/1 blue Merfolk Wizard creature token."

    @Test
    @DisplayName("Tapping it and accepting creates a Merfolk Wizard token")
    void tappingAcceptCreatesToken() {
        Permanent schoolmaster = harness.addToBattlefieldAndReturn(player1, new StonybrookSchoolmaster());

        tap(schoolmaster);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(tokenCount()).isEqualTo(1);
        Permanent token = findPermanents(player1, "Merfolk Wizard").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColors()).containsExactly(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.MERFOLK, CardSubtype.WIZARD);
    }

    @Test
    @DisplayName("Declining the trigger creates no token")
    void decliningCreatesNoToken() {
        Permanent schoolmaster = harness.addToBattlefieldAndReturn(player1, new StonybrookSchoolmaster());

        tap(schoolmaster);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
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

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(tokenCount(player1)).isZero();
        assertThat(tokenCount(player2)).isEqualTo(1);
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
