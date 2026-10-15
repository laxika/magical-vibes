package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.ChatterOfTheSquirrel;
import com.github.laxika.magicalvibes.cards.w.WingsOfVelisVel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NutCollector.class, ChatterOfTheSquirrel.class, WingsOfVelisVel.class})
class NutCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger may create a Squirrel token")
    void upkeepCreatesSquirrelToken() {
        harness.addToBattlefield(player1, new NutCollector());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countSquirrelTokens(player1)).isEqualTo(1);
        Permanent squirrel = findPermanent(player1, "Squirrel");
        assertThat(squirrel.getCard().isToken()).isTrue();
        assertThat(squirrel.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(squirrel.getCard().getPower()).isEqualTo(1);
        assertThat(squirrel.getCard().getToughness()).isEqualTo(1);
        assertThat(squirrel.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(squirrel.getCard().getSubtypes()).containsExactly(CardSubtype.SQUIRREL);
    }

    @Test
    @DisplayName("Declining the upkeep trigger creates no Squirrel token")
    void upkeepDeclinedCreatesNoSquirrelToken() {
        harness.addToBattlefield(player1, new NutCollector());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countSquirrelTokens(player1)).isZero();
    }

    @Test
    @DisplayName("Upkeep trigger does not occur during an opponent's upkeep")
    void upkeepDoesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new NutCollector());

        advanceToUpkeep(player2);

        assertThat(countSquirrelTokens(player1)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Threshold gives all Squirrels +2/+2")
    void thresholdBoostsAllSquirrels() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new NutCollector());
        createSquirrelToken(player2);

        Permanent squirrel = findPermanent(player2, "Squirrel");

        assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(3);

        Permanent nutCollector = findPermanent(player1, "Nut Collector");
        assertThat(gqs.getEffectivePower(gd, nutCollector)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nutCollector)).isEqualTo(1);
    }

    @Test
    @DisplayName("Squirrels lose the threshold boost below seven cards")
    void thresholdBoostStopsBelowSevenCards() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new NutCollector());
        createSquirrelToken(player1);

        Permanent squirrel = findPermanent(player1, "Squirrel");
        assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(3);

        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));

        assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new NutCollector());
        createSquirrelToken(player1);

        Permanent squirrel = findPermanent(player1, "Squirrel");

        assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(1);
    }

    @Test
    @DisplayName("Threshold also boosts Nut Collector when it becomes a Squirrel")
    void thresholdBoostsCollectorWhenItBecomesASquirrel() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent collector = harness.addToBattlefieldAndReturn(player1, new NutCollector());
        harness.setHand(player1, List.of(new WingsOfVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, collector.getId());

        assertThat(gqs.getEffectivePower(gd, collector)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, collector)).isEqualTo(6);
    }

    @Test
    @DisplayName("A Squirrel created by the upkeep trigger immediately receives threshold bonuses")
    void upkeepTokenReceivesThresholdBonuses() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new NutCollector());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent squirrel = findPermanent(player1, "Squirrel");
        assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(3);
    }

    private void createSquirrelToken(Player tokenController) {
        harness.forceActivePlayer(tokenController);
        harness.castFromHand(tokenController, new ChatterOfTheSquirrel(), "{G}");
        harness.passBothPriorities();
    }

    private long countSquirrelTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SQUIRREL))
                .count();
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new ChatterOfTheSquirrel(), new ChatterOfTheSquirrel(), new ChatterOfTheSquirrel(),
                new ChatterOfTheSquirrel(), new ChatterOfTheSquirrel(), new ChatterOfTheSquirrel(),
                new ChatterOfTheSquirrel());
    }
}
