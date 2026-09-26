package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CombineChrysalis.class, GrizzlyBears.class})
class CombineChrysalisTest extends BaseCardTest {

    @Test
    @DisplayName("Own creature tokens have flying")
    void givesFlyingToOwnCreatureTokensOnly() {
        harness.addToBattlefield(player1, new CombineChrysalis());
        Permanent ownToken = harness.addToBattlefieldAndReturn(player1, createTokenCreature("Soldier Token"));
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentToken = harness.addToBattlefieldAndReturn(player2, createTokenCreature("Opponent Token"));

        assertThat(gqs.hasKeyword(gd, ownToken, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentToken, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing a token creates a 4/4 green Beast at sorcery speed")
    void sacrificesTokenToCreateBeast() {
        Permanent chrysalis = harness.addToBattlefieldAndReturn(player1, new CombineChrysalis());
        Permanent token = harness.addToBattlefieldAndReturn(player1, createTokenCreature("Soldier Token"));
        addManaForAbility();
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(token.getId()));
        Permanent beast = findPermanent(player1, "Beast");
        assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(beast.getEffectivePower()).isEqualTo(4);
        assertThat(beast.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isTrue();
        assertThat(chrysalis.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot be activated without a token to sacrifice")
    void requiresTokenToSacrifice() {
        harness.addToBattlefield(player1, new CombineChrysalis());
        addManaForAbility();
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("a token");
    }

    @Test
    @DisplayName("The ability can only be activated at sorcery speed")
    void sorcerySpeedOnly() {
        harness.addToBattlefield(player1, new CombineChrysalis());
        harness.addToBattlefield(player1, createTokenCreature("Soldier Token"));
        addManaForAbility();
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Card createTokenCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
