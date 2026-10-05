package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MajesticMyriarch.class, GrizzlyBears.class, SerraAngel.class})
class MajesticMyriarchTest extends BaseCardTest {

    private Permanent addMyriarch(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new MajesticMyriarch());
        perm.setSummoningSick(false);
        return perm;
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void endTurn() {
        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("P/T is 2/2 when it is your only creature")
    void isTwoTwoWhenAlone() {
        Permanent myriarch = addMyriarch(player1);

        assertThat(gqs.getEffectivePower(gd, myriarch)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, myriarch)).isEqualTo(2);
    }

    @Test
    @DisplayName("P/T is twice the number of creatures you control")
    void ptIsTwiceControlledCreatures() {
        Permanent myriarch = addMyriarch(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, myriarch)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, myriarch)).isEqualTo(6);
    }

    @Test
    @DisplayName("Opponent creatures do not count toward P/T")
    void opponentCreaturesDoNotCount() {
        Permanent myriarch = addMyriarch(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, myriarch)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, myriarch)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gains flying until end of turn when you control a creature with flying")
    void gainsFlyingFromAllyFlier() {
        Permanent myriarch = addMyriarch(player1);
        harness.addToBattlefield(player1, new SerraAngel());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not gain flying when only the opponent controls a flier")
    void opponentFlierDoesNotGrant() {
        Permanent myriarch = addMyriarch(player1);
        harness.addToBattlefield(player2, new SerraAngel());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not gain keywords when you control no matching creatures")
    void noKeywordsWithoutMatchers() {
        Permanent myriarch = addMyriarch(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void keywordsWearOffAtEndOfTurn() {
        Permanent myriarch = addMyriarch(player1);
        harness.addToBattlefield(player1, new SerraAngel());

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.FLYING)).isTrue();

        endTurn();

        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Triggers during the opponent's combat as well")
    void triggersDuringOpponentCombat() {
        Permanent myriarch = addMyriarch(player1);
        harness.addToBattlefield(player1, new SerraAngel());

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The single combat ability triggers even without matching keywords")
    void alwaysTriggersAtCombat() {
        addMyriarch(player1);

        advanceToCombat(player1);

        assertThat(gd.stack).hasSize(1);
    }

    @ParameterizedTest
    @EnumSource(value = Keyword.class, names = {
            "FLYING", "FIRST_STRIKE", "DOUBLE_STRIKE", "DEATHTOUCH", "HASTE", "HEXPROOF",
            "INDESTRUCTIBLE", "LIFELINK", "MENACE", "REACH", "TRAMPLE", "VIGILANCE"
    })
    @DisplayName("Each listed keyword is gained and retained after its donor leaves")
    void gainsEveryListedKeywordUntilEndOfTurn(Keyword keyword) {
        Permanent myriarch = addMyriarch(player1);
        Permanent donor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        donor.getGrantedKeywords().add(keyword);

        advanceToCombat(player1);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(donor);

        assertThat(gqs.hasKeyword(gd, myriarch, keyword)).isTrue();

        endTurn();

        assertThat(gqs.hasKeyword(gd, myriarch, keyword)).isFalse();
    }

    @Test
    @DisplayName("A creature gaining a new keyword in response is considered on resolution")
    void gainsKeywordAcquiredInResponse() {
        Permanent myriarch = addMyriarch(player1);
        Permanent donor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerraAngel());

        advanceToCombat(player1);
        donor.getGrantedKeywords().add(Keyword.TRAMPLE);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A flier entering in response supplies flying to the combat ability")
    void gainsFlyingFromCreatureEnteringInResponse() {
        Permanent myriarch = addMyriarch(player1);

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, new SerraAngel());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("A donor leaving before resolution does not supply its keywords")
    void doesNotGainKeywordsFromDonorLeavingInResponse() {
        Permanent myriarch = addMyriarch(player1);
        Permanent donor = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        advanceToCombat(player1);
        gd.playerBattlefields.get(player1.getId()).remove(donor);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A flier entering after resolution does not retroactively grant flying")
    void doesNotGainFlyingFromCreatureEnteringAfterResolution() {
        Permanent myriarch = addMyriarch(player1);
        Permanent donor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        donor.getGrantedKeywords().add(Keyword.TRAMPLE);

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new SerraAngel());

        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, myriarch, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Power and toughness update as creatures enter and leave")
    void powerAndToughnessTrackTheCurrentCreatureCount() {
        Permanent myriarch = addMyriarch(player1);
        Permanent donor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, myriarch)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, myriarch)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(donor);

        assertThat(gqs.getEffectivePower(gd, myriarch)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, myriarch)).isEqualTo(2);
    }

    @Test
    @DisplayName("The characteristic ability works in hand and graveyard without counting itself")
    void powerAndToughnessAreDefinedOutsideTheBattlefield() {
        MajesticMyriarch myriarch = new MajesticMyriarch();
        harness.setHand(player1, java.util.List.of(myriarch));

        assertThat(gqs.getEffectiveCardPower(gd, myriarch)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, myriarch)).isZero();

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());

        assertThat(gqs.getEffectiveCardPower(gd, myriarch)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, myriarch)).isEqualTo(2);

        gd.playerHands.get(player1.getId()).remove(myriarch);
        gd.playerGraveyards.get(player1.getId()).add(myriarch);

        assertThat(gqs.getEffectiveCardPower(gd, myriarch)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, myriarch)).isEqualTo(2);
    }
}
