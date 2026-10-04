package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.k.KnightWatch;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IllnessInTheRanks.class, KnightWatch.class})
class IllnessInTheRanksTest extends BaseCardTest {

    @Test
    @DisplayName("Creature tokens get -1/-1 regardless of controller")
    void debuffsCreatureTokens() {
        harness.addToBattlefield(player1, new IllnessInTheRanks());
        Permanent ownToken = harness.addToBattlefieldAndReturn(player1, createTokenCreature("Soldier Token", 2, 2));
        Permanent opponentToken = harness.addToBattlefieldAndReturn(player2, createTokenCreature("Zombie Token", 3, 3));

        assertThat(gqs.getEffectivePower(gd, ownToken)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownToken)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentToken)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentToken)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nontoken creatures are unaffected")
    void doesNotDebuffNontokenCreatures() {
        harness.addToBattlefield(player1, new IllnessInTheRanks());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, createCreature("Grizzly Bears", 2, 2));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("A 1/1 creature token dies after the static debuff is applied")
    void oneOneTokenDiesToDebuff() {
        harness.addToBattlefield(player1, new IllnessInTheRanks());
        harness.addToBattlefield(player1, createTokenCreature("Soldier Token", 1, 1));

        harness.runStateBasedActions();

        assertThat(findPermanents(player1, "Soldier Token")).isEmpty();
    }

    @Test
    @DisplayName("Tokens created after Illness resolves are immediately debuffed")
    void debuffsNewlyCreatedTokens() {
        harness.castFromHand(player1, new IllnessInTheRanks(), "{B}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new KnightWatch(), "{4}{W}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Knight")).hasSize(2).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Copies controlled by different players cumulatively kill 2/2 tokens")
    void multipleCopiesStack() {
        harness.castFromHand(player1, new KnightWatch(), "{4}{W}");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Knight")).hasSize(2);

        harness.addToBattlefield(player1, new IllnessInTheRanks());
        harness.addToBattlefield(player2, new IllnessInTheRanks());
        harness.runStateBasedActions();

        assertThat(findPermanents(player1, "Knight")).isEmpty();
    }

    @Test
    @DisplayName("An animated token copy is affected by its own ability")
    void debuffsItsOwnCreatureTokenCopy() {
        IllnessInTheRanks tokenCopy = new IllnessInTheRanks();
        tokenCopy.setToken(true);
        tokenCopy.setAdditionalTypes(java.util.Set.of(CardType.CREATURE));
        tokenCopy.setPower(2);
        tokenCopy.setToughness(2);
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, tokenCopy);

        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(1);
    }

    private Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private Card createTokenCreature(String name, int power, int toughness) {
        Card card = createCreature(name, power, toughness);
        card.setToken(true);
        return card;
    }
}
