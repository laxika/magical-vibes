package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SnarlingWolf;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodvialPurveyor.class, SnarlingWolf.class})
class BloodvialPurveyorTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent casting a spell gives that opponent a Blood token")
    void opponentCastingSpellCreatesBloodForOpponent() {
        addCreatureReady(player1, new BloodvialPurveyor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new SnarlingWolf(), "{G}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Blood")).isZero();
        assertThat(countPermanents(player2, "Blood")).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking gets +1/+0 for each Blood token controlled by the defending player")
    void attackBoostsPerDefendingPlayersBloodTokens() {
        Permanent purveyor = addCreatureReady(player1, new BloodvialPurveyor());
        addBloodToken(player1);
        addBloodToken(player2);
        addBloodToken(player2);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(purveyor)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, purveyor)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, purveyor)).isEqualTo(6);
    }

    @Test
    @DisplayName("The attack boost wears off at cleanup")
    void attackBoostWearsOffAtCleanup() {
        Permanent purveyor = addCreatureReady(player1, new BloodvialPurveyor());
        addBloodToken(player2);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(purveyor)));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, purveyor)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, purveyor)).isEqualTo(5);
    }

    @Test
    @DisplayName("The controller casting a spell creates no Blood token")
    void controllerCastingSpellCreatesNoBlood() {
        addCreatureReady(player1, new BloodvialPurveyor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new SnarlingWolf(), "{G}");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blood")).isZero();
        assertThat(countPermanents(player2, "Blood")).isZero();
    }

    @Test
    @DisplayName("The Blood trigger resolves even after Purveyor leaves the battlefield")
    void bloodTriggerSurvivesSourceLeaving() {
        addCreatureReady(player1, new BloodvialPurveyor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new SnarlingWolf(), "{G}");

        gd.playerBattlefields.get(player1.getId()).clear();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blood")).isZero();
        assertThat(countPermanents(player2, "Blood")).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking without defending player's Blood tokens gives no boost")
    void attackWithoutDefendingBloodGivesNoBoost() {
        Permanent purveyor = addCreatureReady(player1, new BloodvialPurveyor());
        addBloodToken(player1);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, purveyor)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, purveyor)).isEqualTo(6);
    }

    @Test
    @DisplayName("Blood is counted at resolution and the resolved boost stays fixed")
    void attackCountsBloodAtResolution() {
        Permanent purveyor = addCreatureReady(player1, new BloodvialPurveyor());
        addBloodToken(player2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(1);

        addBloodToken(player2);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, purveyor)).isEqualTo(7);

        gd.playerBattlefields.get(player2.getId()).clear();
        assertThat(gqs.getEffectivePower(gd, purveyor)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, purveyor)).isEqualTo(6);
    }

    private void addBloodToken(com.github.laxika.magicalvibes.model.Player player) {
        Card bloodCard = new Card();
        bloodCard.setName("Blood");
        bloodCard.setType(CardType.ARTIFACT);
        bloodCard.setManaCost("");
        bloodCard.setToken(true);
        bloodCard.setSubtypes(List.of(CardSubtype.BLOOD));
        bloodCard.addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new DiscardCardTypeCost(null, null), new SacrificeSelfCost(), new DrawCardEffect()),
                "{1}, {T}, Discard a card, Sacrifice this token: Draw a card."
        ));
        Permanent blood = new Permanent(bloodCard);
        blood.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(blood);
    }
}
