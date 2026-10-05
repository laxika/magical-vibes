package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NaturesEmbrace.class, Forest.class, GrizzlyBears.class, FountainOfYouth.class})
class NaturesEmbraceTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAuraOn(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted land gains an ability that adds two mana of any one color")
    void enchantedLandGainsManaAbility() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        castAuraOn(forest);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Nature's Embrace cannot enchant a noncreature nonland permanent")
    void cannotEnchantNonCreatureNonLand() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new NaturesEmbrace()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or land");
    }

    @Test
    @DisplayName("An opponent's enchanted land gives mana to its controller")
    void opponentsLandProducesManaForItsController() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        castAuraOn(forest);
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "BLUE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing Nature's Embrace ends the creature boost")
    void removingAuraEndsCreatureBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAuraOn(bears);
        Permanent aura = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Nature's Embrace"));

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The enchanted land retains its original mana ability")
    void enchantedLandRetainsOriginalManaAbility() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        castAuraOn(forest);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(forest.isTapped()).isTrue();
    }
    private void castAuraOn(Permanent target) {
        harness.setHand(player1, List.of(new NaturesEmbrace()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
