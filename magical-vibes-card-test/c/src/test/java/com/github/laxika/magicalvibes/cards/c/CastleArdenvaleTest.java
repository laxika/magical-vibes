package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CastleArdenvale.class, Plains.class})
class CastleArdenvaleTest extends BaseCardTest {

    @Test
    void opponentsPlainsDoesNotAllowUntappedEntry() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new CastleArdenvale()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Castle Ardenvale").isTapped()).isTrue();
    }

    @Test
    void tappedPlainsAllowsUntappedEntry() {
        harness.addToBattlefieldAndReturn(player1, new Plains()).tap();
        harness.setHand(player1, List.of(new CastleArdenvale()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Castle Ardenvale").isTapped()).isFalse();
    }

    @Test
    void tokenCreationUsesStackAndPaysFullCost() {
        Permanent castle = harness.addToBattlefieldAndReturn(player1, new CastleArdenvale());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(castle.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(countPermanents(player1, "Human")).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Human")).hasSize(1);
        assertThat(countPermanents(player2, "Human")).isZero();
        Permanent token = findPermanent(player1, "Human");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.isTapped()).isFalse();
    }

    @Test
    void tokenAbilityRequiresTwoWhiteMana() {
        Permanent castle = harness.addToBattlefieldAndReturn(player1, new CastleArdenvale());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(castle.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Human")).isZero();
    }

    @Test
    @DisplayName("Enters tapped without a Plains")
    void entersTappedWithoutPlains() {
        harness.setHand(player1, List.of(new CastleArdenvale()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Castle Ardenvale").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Plains")
    void entersUntappedWithPlains() {
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new CastleArdenvale()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Castle Ardenvale").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana ability adds white mana")
    void manaAbilityAddsWhite() {
        harness.addToBattlefield(player1, new CastleArdenvale());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = findPermanent(player1, "Castle Ardenvale");
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Token ability creates a Human token")
    void tokenAbilityCreatesHumanToken() {
        harness.addToBattlefield(player1, new CastleArdenvale());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> "Human".equals(permanent.getCard().getName()));
    }
}
