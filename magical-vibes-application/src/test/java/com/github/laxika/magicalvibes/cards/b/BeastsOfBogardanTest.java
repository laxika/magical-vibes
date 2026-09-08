package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BeastsOfBogardanTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    @Test
    @DisplayName("Base 3/3 when no opponent controls a nontoken white permanent")
    void baseWithoutWhitePermanent() {
        harness.addToBattlefield(player1, new BeastsOfBogardan());

        Permanent beasts = findPermanent(player1, "Beasts of Bogardan");
        assertThat(gqs.getEffectivePower(gd, beasts)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beasts)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 when an opponent controls a nontoken white permanent")
    void boostWhenOpponentControlsWhitePermanent() {
        harness.addToBattlefield(player1, new BeastsOfBogardan());
        harness.addToBattlefield(player2, createCreature("Serra Angel", 4, 4, CardColor.WHITE));

        Permanent beasts = findPermanent(player1, "Beasts of Bogardan");
        assertThat(gqs.getEffectivePower(gd, beasts)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, beasts)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not get the boost from a white token")
    void noBoostFromWhiteToken() {
        harness.addToBattlefield(player1, new BeastsOfBogardan());
        Card token = createCreature("Soldier Token", 1, 1, CardColor.WHITE);
        token.setToken(true);
        harness.addToBattlefield(player2, token);

        Permanent beasts = findPermanent(player1, "Beasts of Bogardan");
        assertThat(gqs.getEffectivePower(gd, beasts)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beasts)).isEqualTo(3);
    }

    @Test
    @DisplayName("The controller's own white permanent does not grant the boost")
    void noBoostFromOwnWhitePermanent() {
        harness.addToBattlefield(player1, new BeastsOfBogardan());
        harness.addToBattlefield(player1, createCreature("Serra Angel", 4, 4, CardColor.WHITE));

        Permanent beasts = findPermanent(player1, "Beasts of Bogardan");
        assertThat(gqs.getEffectivePower(gd, beasts)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beasts)).isEqualTo(3);
    }

    @Test
    @DisplayName("Red creature cannot block Beasts of Bogardan")
    void redCreatureCannotBlock() {
        Permanent attacker = new Permanent(new BeastsOfBogardan());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreature("Goblin Raider", 2, 2, CardColor.RED));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }
}
