package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.k.KavuAggressor;
import com.github.laxika.magicalvibes.cards.s.ShivanHarvest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PureReflection.class, KavuAggressor.class, ShivanHarvest.class})
class PureReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("A creature spell destroys all Reflections and creates a token sized to its mana value")
    void creatureSpellReplacesAllReflections() {
        harness.addToBattlefield(player1, new PureReflection());
        Permanent oldReflection = harness.addToBattlefieldAndReturn(player1, reflectionToken());
        harness.addToBattlefield(player2, reflectionToken());
        harness.setHand(player1, List.of(new KavuAggressor()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(oldReflection);
        assertThat(countPermanents(player1, "Reflection")).isEqualTo(1);
        assertThat(countPermanents(player2, "Reflection")).isZero();
        Permanent reflection = findPermanent(player1, "Reflection");
        assertThat(reflection.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(reflection.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(reflection.getCard().getSubtypes()).containsExactly(CardSubtype.REFLECTION);
        assertThat(gqs.getEffectivePower(gd, reflection)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, reflection)).isEqualTo(3);
    }

    @Test
    @DisplayName("The player who casts the creature spell controls the Reflection token")
    void tokenIsCreatedForCastingPlayer() {
        harness.addToBattlefield(player1, new PureReflection());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new KavuAggressor()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Reflection")).isZero();
        assertThat(countPermanents(player2, "Reflection")).isEqualTo(1);
    }

    @Test
    @DisplayName("A noncreature spell does not trigger Pure Reflection")
    void noncreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new PureReflection());
        Permanent ownReflection = harness.addToBattlefieldAndReturn(player1, reflectionToken());
        Permanent opponentReflection = harness.addToBattlefieldAndReturn(player2, reflectionToken());
        harness.setHand(player1, List.of(new ShivanHarvest()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Reflection")).isSameAs(ownReflection);
        assertThat(findPermanent(player2, "Reflection")).isSameAs(opponentReflection);
    }

    @Test
    @DisplayName("The Reflection size uses mana value rather than additional kicker costs")
    void tokenSizeUsesManaValueNotKickerCost() {
        harness.addToBattlefield(player1, new PureReflection());
        harness.setHand(player1, List.of(new KavuAggressor()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent reflection = findPermanent(player1, "Reflection");
        assertThat(gqs.getEffectivePower(gd, reflection)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, reflection)).isEqualTo(3);
    }

    private Card reflectionToken() {
        Card token = new Card();
        token.setName("Reflection");
        token.setType(CardType.CREATURE);
        token.setManaCost("");
        token.setColor(CardColor.WHITE);
        token.setSubtypes(List.of(CardSubtype.REFLECTION));
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        return token;
    }
}
