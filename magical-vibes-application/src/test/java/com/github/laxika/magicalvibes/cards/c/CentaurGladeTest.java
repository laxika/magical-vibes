package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CentaurGlade.class)
class CentaurGladeTest extends BaseCardTest {

    @Test
    void activatingAbilityCreatesCentaurToken() {
        harness.addToBattlefield(player1, new CentaurGlade());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Centaur");
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CENTAUR);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isSummoningSick()).isTrue();
    }

    @Test
    void canActivateAbilityMultipleTimes() {
        harness.addToBattlefield(player1, new CentaurGlade());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Centaur")).isEqualTo(2);
    }

    @Test
    void tokenIsCreatedOnlyWhenAbilityResolves() {
        Permanent glade = harness.addToBattlefieldAndReturn(player1, new CentaurGlade());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(countPermanents(player1, "Centaur")).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(glade.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Centaur")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationRequiresTwoGreenMana() {
        harness.addToBattlefield(player1, new CentaurGlade());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Centaur")).isZero();
    }

    @Test
    void opponentCanCreateTokenDuringOtherPlayersTurn() {
        harness.addToBattlefield(player2, new CentaurGlade());
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Centaur")).isEqualTo(1);
        assertThat(countPermanents(player1, "Centaur")).isZero();
    }
}
