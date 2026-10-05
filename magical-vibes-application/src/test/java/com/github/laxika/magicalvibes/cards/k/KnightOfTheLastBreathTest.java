package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.ImpassionedOrator;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightOfTheLastBreath.class, ImpassionedOrator.class, KayasWrath.class})
class KnightOfTheLastBreathTest extends BaseCardTest {

    @Test
    @DisplayName("Afterlife 3 creates three 1/1 white and black Spirit tokens with flying")
    void afterlifeCreatesThreeSpiritTokens() {
        harness.addToBattlefield(player1, new KnightOfTheLastBreath());
        harness.setHand(player1, List.of(new KayasWrath()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Knight of the Last Breath");
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Spirit"))
                .toList();

        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    @DisplayName("Sacrificing another nontoken creature creates a Spirit token")
    void sacrificesAnotherNontokenCreatureToCreateSpiritToken() {
        harness.addToBattlefield(player1, new KnightOfTheLastBreath());
        harness.addToBattlefield(player1, new ImpassionedOrator());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Impassioned Orator");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Spirit")))
                .hasSize(1);
    }

    @Test
    @DisplayName("Cannot sacrifice Knight of the Last Breath itself")
    void cannotSacrificeItself() {
        harness.addToBattlefield(player1, new KnightOfTheLastBreath());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing a second Knight pays the cost immediately and triggers its afterlife")
    void sacrificingAnotherKnightCreatesFourSpirits() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new KnightOfTheLastBreath());
        source.setTapped(true);
        harness.addToBattlefield(player1, new KnightOfTheLastBreath());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Knight of the Last Breath");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Spirit")))
                .hasSize(4)
                .allSatisfy(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
                    assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
                    assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
                });
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Spirit tokens cannot pay the nontoken sacrifice cost")
    void cannotSacrificeSpiritTokens() {
        harness.addToBattlefield(player1, new KnightOfTheLastBreath());
        harness.addToBattlefield(player1, new KnightOfTheLastBreath());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Spirit")))
                .hasSize(4);
    }

    @Test
    @DisplayName("An opponent's nontoken creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new KnightOfTheLastBreath());
        harness.addToBattlefield(player2, new KnightOfTheLastBreath());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Knight of the Last Breath");
        harness.assertOnBattlefield(player2, "Knight of the Last Breath");
    }

    @Test
    @DisplayName("The sacrifice ability requires three mana as well as another nontoken creature")
    void cannotActivateWithInsufficientMana() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new KnightOfTheLastBreath());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new KnightOfTheLastBreath());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source, sacrifice);
        harness.assertNotInGraveyard(player1, "Knight of the Last Breath");
    }
}
