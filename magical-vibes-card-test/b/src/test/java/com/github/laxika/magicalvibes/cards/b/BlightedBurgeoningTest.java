package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CopperHostCrusher;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlightedBurgeoning.class, Forest.class, CopperHostCrusher.class})
class BlightedBurgeoningTest extends BaseCardTest {

    @Test
    @DisplayName("When Blighted Burgeoning enters, it creates an Incubator with two +1/+1 counters")
    void enteringCreatesIncubator() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new BlightedBurgeoning()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        resolveAllTriggers();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> "Blighted Burgeoning".equals(permanent.getCard().getName())
                        && forest.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Tapping the enchanted land adds one mana of any color")
    void enchantedLandAddsAnyColorMana() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new BlightedBurgeoning());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.tapPermanent(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blighted Burgeoning can target only a land")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new CopperHostCrusher());
        Permanent bears = findPermanent(player1, "Copper Host Crusher");
        harness.setHand(player1, List.of(new BlightedBurgeoning()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    void incubatorHasItsArtifactSubtype() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = findPermanent(player1, "Forest");
        harness.setHand(player1, List.of(new BlightedBurgeoning()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Incubator").getCard().getSubtypes())
                .extracting(Enum::name)
                .contains("INCUBATOR");
    }

    @Test
    void incubatorTransformsAndRetainsCounters() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = findPermanent(player1, "Forest");
        harness.setHand(player1, List.of(new BlightedBurgeoning()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, forest.getId());
        resolveAllTriggers();
        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        resolveAllTriggers();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(gqs.isArtifact(gd, incubator)).isTrue();
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(2);
        assertThat(incubator.getCard().getActivatedAbilities()).isEmpty();
    }

    @Test
    void opponentsEnchantedLandGivesManaToItsController() {
        harness.addToBattlefield(player2, new Forest());
        Permanent forest = findPermanent(player2, "Forest");
        harness.setHand(player1, List.of(new BlightedBurgeoning()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, forest.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Incubator")).hasSize(1);
        assertThat(findPermanents(player2, "Incubator")).isEmpty();
        harness.tapPermanent(player2, 0);
        harness.handleListChoice(player2, "RED");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unenchantedLandDoesNotAddExtraMana() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new BlightedBurgeoning()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, forest.getId());
        resolveAllTriggers();

        harness.tapPermanent(player1, 1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
