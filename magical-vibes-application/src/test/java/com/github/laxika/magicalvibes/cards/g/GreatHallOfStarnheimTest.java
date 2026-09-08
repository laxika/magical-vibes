package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GreatHallOfStarnheimTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and taps for black mana")
    void entersTappedAndTapsForBlack() {
        harness.setHand(player1, List.of(new GreatHallOfStarnheim()));
        harness.playLand(player1, 0);
        Permanent land = findPermanent(player1, "Great Hall of Starnheim");

        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifices itself and a creature to create a 4/4 flying vigilant Angel Warrior")
    void createsAngelWarriorAfterPayingCosts() {
        harness.addToBattlefield(player1, new GreatHallOfStarnheim());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Great Hall of Starnheim");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        Permanent token = findPermanent(player1, "Angel Warrior");
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ANGEL, CardSubtype.WARRIOR);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING, Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Cannot activate the token ability without a creature to sacrifice")
    void requiresCreatureToSacrifice() {
        harness.addToBattlefield(player1, new GreatHallOfStarnheim());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
