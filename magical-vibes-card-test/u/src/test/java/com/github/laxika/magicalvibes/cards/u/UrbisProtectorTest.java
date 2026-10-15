package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrbisProtector.class})
class UrbisProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 4/4 white Angel token with flying")
    void etbCreatesAngelToken() {
        harness.setHand(player1, List.of(new UrbisProtector()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent angel = findPermanent(player1, "Angel");
        assertThat(angel.getCard().isToken()).isTrue();
        assertThat(angel.getCard().getPower()).isEqualTo(4);
        assertThat(angel.getCard().getToughness()).isEqualTo(4);
        assertThat(angel.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(angel.getCard().getSubtypes()).contains(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Noncast entry creates exactly one untapped Angel for the entering creature's controller")
    void noncastEntryCreatesTokenForController() {
        harness.enterBattlefieldAndReturn(player2, new UrbisProtector());

        harness.assertNotOnBattlefield(player2, "Angel");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
        assertThat(findPermanent(player2, "Angel").isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Angel");
    }

    @Test
    @DisplayName("The Angel is still created if Urbis Protector dies before its trigger resolves")
    void triggerResolvesAfterSourceDies() {
        Permanent protector = harness.enterBattlefieldAndReturn(player1, new UrbisProtector());
        protector.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Urbis Protector");
        harness.assertNotOnBattlefield(player1, "Angel");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Angel");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }
}
