package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.RiteOfReplication;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Bone-Cairn Butcher")
@CardUsed({BoneCairnButcher.class, RiteOfReplication.class})
class BoneCairnButcherTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates two tapped and attacking Warrior tokens with deathtouch")
    void attackingCreatesTwoDeathtouchWarriorTokens() {
        addCreatureReady(player1, new BoneCairnButcher());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        List<Permanent> tokens = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttackedThisTurn()).isFalse();
            assertThat(token.isAttacking()).isTrue();
            assertThat(gqs.hasKeyword(gd, token, Keyword.DEATHTOUCH)).isTrue();
        });
    }

    @Test
    @DisplayName("Mobilized tokens are sacrificed at the beginning of the next end step")
    void mobilizedTokensAreSacrificedAtNextEndStep() {
        addCreatureReady(player1, new BoneCairnButcher());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(2);

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    @DisplayName("An attacking token copy grants itself deathtouch")
    void attackingTokenCopyHasDeathtouch() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new BoneCairnButcher());
        harness.setHand(player1, List.of(new RiteOfReplication()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, original.getId());
        harness.passBothPriorities();

        Permanent copy = findPermanent(player1, "Bone-Cairn Butcher");
        copy.setSummoningSick(false);
        assertThat(gqs.hasKeyword(gd, copy, Keyword.DEATHTOUCH)).isFalse();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(copy.isAttacking()).isTrue();
        assertThat(gqs.hasKeyword(gd, copy, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Warrior tokens lose deathtouch when they stop attacking")
    void tokensLoseDeathtouchAfterCombat() {
        Permanent butcher = addCreatureReady(player1, new BoneCairnButcher());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        List<Permanent> tokens = findPermanents(player1, "Warrior");
        assertThat(tokens).hasSize(2).allSatisfy(token ->
                assertThat(gqs.hasKeyword(gd, token, Keyword.DEATHTOUCH)).isTrue());
        assertThat(gqs.hasKeyword(gd, butcher, Keyword.DEATHTOUCH)).isFalse();

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.isAttacking()).isFalse();
            assertThat(gqs.hasKeyword(gd, token, Keyword.DEATHTOUCH)).isFalse();
        });
    }

    @Test
    @DisplayName("Removing the Butcher removes deathtouch but does not prevent the delayed sacrifice")
    void delayedSacrificeSurvivesSourceRemoval() {
        Permanent butcher = addCreatureReady(player1, new BoneCairnButcher());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        List<Permanent> tokens = findPermanents(player1, "Warrior");
        assertThat(tokens).hasSize(2);
        gd.playerBattlefields.get(player1.getId()).remove(butcher);

        assertThat(tokens).allSatisfy(token ->
                assertThat(gqs.hasKeyword(gd, token, Keyword.DEATHTOUCH)).isFalse());
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior")).isEmpty();
    }
}
