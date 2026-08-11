package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DruidsCall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NutCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger may create a Squirrel token")
    void upkeepCreatesSquirrelToken() {
        harness.addToBattlefield(player1, new NutCollector());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countSquirrelTokens(player1)).isEqualTo(1);
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
    @DisplayName("Threshold gives all Squirrels +2/+2")
    void thresholdBoostsAllSquirrels() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new NutCollector());
        createSquirrelToken(player2);

        Permanent squirrel = findPermanent(player2, "Squirrel");

        assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(3);
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

    private void createSquirrelToken(Player tokenController) {
        Permanent target = addCreatureReady(tokenController, new HillGiant());
        harness.setHand(player1, List.of(new DruidsCall()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private long countSquirrelTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SQUIRREL))
                .count();
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
    }
}
