package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({DragonEgg.class, PlanarCleansing.class, Shock.class, Disperse.class})
class DragonEggTest extends BaseCardTest {

    @Test
    @DisplayName("When Dragon Egg dies, a 2/2 red flying Dragon token is created")
    void deathTriggerCreatesDragonToken() {
        harness.addToBattlefield(player1, new DragonEgg());

        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities(); // Death trigger resolves

        List<Permanent> tokens = findPermanents(player1, "Dragon");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("The Dragon token's {R} ability gives it +1/+0 until end of turn")
    void dragonTokenHasFirebreathing() {
        harness.addToBattlefield(player1, new DragonEgg());

        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanents(player1, "Dragon").getFirst());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, tokenIndex, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Dragon").getFirst();
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(token.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Returning Dragon Egg to hand does not create a token")
    void bounceDoesNotTriggerDeathAbility() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new DragonEgg());
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, egg.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Dragon Egg")).isZero();
        assertThat(countPermanents(player1, "Dragon")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof DragonEgg);
    }

    @Test
    @DisplayName("Each player gets a Dragon for each Dragon Egg they controlled when it died")
    void simultaneousDeathsCreateTokensForBothControllers() {
        harness.addToBattlefield(player1, new DragonEgg());
        harness.addToBattlefield(player1, new DragonEgg());
        harness.addToBattlefield(player2, new DragonEgg());
        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Dragon")).isEqualTo(2);
        assertThat(countPermanents(player2, "Dragon")).isEqualTo(1);
        assertThat(countPermanents(player1, "Dragon Egg")).isZero();
        assertThat(countPermanents(player2, "Dragon Egg")).isZero();
    }

    @Test
    @DisplayName("A newly created tapped Dragon can activate firebreathing repeatedly")
    void firebreathingStacksWithoutTappingOrWaitingForNextTurn() {
        Permanent egg = harness.addToBattlefieldAndReturn(player1, new DragonEgg());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, egg.getId());
        resolveAllTriggers();
        Permanent token = findPermanent(player1, "Dragon");
        token.setTapped(true);
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, tokenIndex, null, null);
        harness.activateAbility(player1, tokenIndex, null, null);
        resolveAllTriggers();

        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.isTapped()).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(token.getEffectivePower()).isEqualTo(2);
    }
}
