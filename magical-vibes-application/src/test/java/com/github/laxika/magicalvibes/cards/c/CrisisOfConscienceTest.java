package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrisisOfConscience.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class})
class CrisisOfConscienceTest extends BaseCardTest {

    @Test
    @DisplayName("The token mode destroys tokens and leaves nontoken permanents alone")
    void destroysAllTokens() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, newToken());
        harness.addToBattlefield(player2, new GloriousAnthem());

        cast(0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Soldier");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("The nonland nontoken mode destroys only matching permanents")
    void destroysNonlandNontokenPermanents() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, newToken());
        harness.addToBattlefield(player2, new GloriousAnthem());

        cast(1);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Soldier");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The token mode destroys land and enchantment tokens on both battlefields")
    void destroysNoncreatureTokensOnBothBattlefields() {
        Forest landToken = new Forest();
        landToken.setToken(true);
        GloriousAnthem enchantmentToken = new GloriousAnthem();
        enchantmentToken.setToken(true);
        harness.addToBattlefield(player1, landToken);
        harness.addToBattlefield(player2, enchantmentToken);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        cast(0);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertNotInGraveyard(player2, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Crisis of Conscience");
    }

    @Test
    @DisplayName("The nonland nontoken mode spares noncreature tokens and destroys both players' creatures")
    void sparesNoncreatureTokensAndDestroysBothPlayersCreatures() {
        Forest landToken = new Forest();
        landToken.setToken(true);
        GloriousAnthem enchantmentToken = new GloriousAnthem();
        enchantmentToken.setToken(true);
        harness.addToBattlefield(player1, landToken);
        harness.addToBattlefield(player2, enchantmentToken);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(1);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The token mode can resolve without any tokens")
    void tokenModeResolvesWithoutTokens() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GloriousAnthem());

        cast(0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Crisis of Conscience");
    }

    @Test
    @DisplayName("The nonland nontoken mode can resolve on an empty battlefield")
    void nonlandNontokenModeResolvesOnEmptyBattlefield() {
        cast(1);

        harness.assertInGraveyard(player1, "Crisis of Conscience");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Both modes respect indestructible while destroying other matching permanents")
    void respectsIndestructible(int modeIndex) {
        Card protectedCard = modeIndex == 0 ? newToken() : new GrizzlyBears();
        Card unprotectedCard = modeIndex == 0 ? newToken() : new GrizzlyBears();
        Permanent protectedPermanent = harness.addToBattlefieldAndReturn(player1, protectedCard);
        protectedPermanent.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.addToBattlefield(player2, unprotectedCard);

        cast(modeIndex);

        harness.assertOnBattlefield(player1, protectedCard.getName());
        harness.assertNotOnBattlefield(player2, unprotectedCard.getName());
        harness.assertNotInGraveyard(player1, protectedCard.getName());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Both modes allow matching creatures to regenerate")
    void allowsRegeneration(int modeIndex) {
        Card protectedCard = modeIndex == 0 ? newToken() : new GrizzlyBears();
        Card unprotectedCard = modeIndex == 0 ? newToken() : new GrizzlyBears();
        Permanent protectedPermanent = harness.addToBattlefieldAndReturn(player1, protectedCard);
        protectedPermanent.setRegenerationShield(1);
        harness.addToBattlefield(player2, unprotectedCard);

        cast(modeIndex);

        harness.assertOnBattlefield(player1, protectedCard.getName());
        harness.assertNotOnBattlefield(player2, unprotectedCard.getName());
        assertThat(protectedPermanent.isTapped()).isTrue();
        assertThat(protectedPermanent.getRegenerationShield()).isZero();
    }

    private Card newToken() {
        Card token = new Card();
        token.setName("Soldier");
        token.setType(CardType.CREATURE);
        token.setPower(2);
        token.setToughness(2);
        token.setToken(true);
        return token;
    }

    private void cast(int modeIndex) {
        harness.setHand(player1, List.of(new CrisisOfConscience()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castModalSorcery(player1, 0, modeIndex, List.of());
        harness.passBothPriorities();
    }
}
