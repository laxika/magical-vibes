package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DeafeningSilence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KenrithsTransformation;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShamblingSuit.class, DeafeningSilence.class, GrizzlyBears.class, Spellbook.class,
        KenrithsTransformation.class})
class ShamblingSuitTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of artifacts and enchantments its controller controls")
    void powerEqualsControlledArtifactsAndEnchantments() {
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new ShamblingSuit());

        assertThat(gqs.getEffectivePower(gd, suit)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, suit)).isEqualTo(3);

        harness.addToBattlefield(player1, new DeafeningSilence());
        assertThat(gqs.getEffectivePower(gd, suit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, suit)).isEqualTo(3);

        harness.addToBattlefield(player1, new Spellbook());
        assertThat(gqs.getEffectivePower(gd, suit)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power ignores non-artifacts, opponent permanents, and updates when permanents leave")
    void powerCountsOnlyMatchingPermanentsControlledByItsController() {
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new ShamblingSuit());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new DeafeningSilence());

        assertThat(gqs.getEffectivePower(gd, suit)).isEqualTo(1);

        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new DeafeningSilence());
        assertThat(gqs.getEffectivePower(gd, suit)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(enchantment);
        assertThat(gqs.getEffectivePower(gd, suit)).isEqualTo(1);
    }

    @Test
    void powerIsDefinedInHandAndGraveyardWithoutCountingItself() {
        ShamblingSuit inHand = new ShamblingSuit();
        ShamblingSuit inGraveyard = new ShamblingSuit();
        harness.setHand(player1, List.of(inHand));
        harness.setGraveyard(player1, List.of(inGraveyard));

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isZero();
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isZero();

        harness.addToBattlefield(player1, new DeafeningSilence());
        harness.addToBattlefield(player2, new ShamblingSuit());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(1);
    }

    @Test
    void countersModifyPowerAndToughnessAfterTheDynamicBase() {
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new ShamblingSuit());
        suit.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.getEffectivePower(gd, suit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, suit)).isEqualTo(5);

        harness.addToBattlefield(player1, new DeafeningSilence());

        assertThat(gqs.getEffectivePower(gd, suit)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, suit)).isEqualTo(5);
    }

    @Test
    void countsCurrentTypesRatherThanPrintedArtifactTypes() {
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new ShamblingSuit());
        Permanent otherSuit = harness.addToBattlefieldAndReturn(player1, new ShamblingSuit());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new KenrithsTransformation());
        aura.setAttachedTo(otherSuit.getId());

        assertThat(gqs.getEffectivePower(gd, suit)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, otherSuit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherSuit)).isEqualTo(3);
    }
}
