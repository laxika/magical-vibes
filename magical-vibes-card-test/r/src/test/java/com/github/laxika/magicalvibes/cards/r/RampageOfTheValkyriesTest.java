package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({RampageOfTheValkyries.class, GrizzlyBears.class})
class RampageOfTheValkyriesTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 4/4 white Angel token with flying and vigilance")
    void enteringBattlefieldCreatesAngel() {
        castRampageAndResolveEtb();

        Permanent angel = findPermanent(player1, "Angel");
        assertThat(angel.getCard().isToken()).isTrue();
        assertThat(angel.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(angel.getCard().getSubtypes()).containsExactly(CardSubtype.ANGEL);
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("When an Angel you control dies, each opponent sacrifices a creature")
    void angelDeathMakesEachOpponentSacrificeCreature() {
        castRampageAndResolveEtb();
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent angel = findPermanent(player1, "Angel");
        angel.setMarkedDamage(4);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Angel");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A non-Angel creature you control dying does not trigger the sacrifice ability")
    void nonAngelDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new RampageOfTheValkyries());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castRampageAndResolveEtb() {
        harness.setHand(player1, List.of(new RampageOfTheValkyries()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
