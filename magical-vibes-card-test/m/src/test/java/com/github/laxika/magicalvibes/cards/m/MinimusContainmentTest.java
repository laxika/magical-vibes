package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FiftyFeetOfRope;
import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MinimusContainment.class, GrizzlyBears.class, Forest.class, FiftyFeetOfRope.class, HillGiantHerdgorger.class})
class MinimusContainmentTest extends BaseCardTest {

    @Test
    @DisplayName("Turns an enchanted creature into a Treasure artifact and removes its abilities")
    void transformsEnchantedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MinimusContainment());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.isArtifact(gd, bears)).isTrue();
        assertThat(gqs.isCreature(gd, bears)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, bears).grantedSubtypes()).contains(CardSubtype.TREASURE);
    }

    @Test
    @DisplayName("The enchanted permanent taps for one chosen color and is sacrificed")
    void enchantedPermanentProducesManaAndIsSacrificed() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MinimusContainment());
        aura.setAttachedTo(bears.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Removing the Aura restores the enchanted permanent")
    void removingAuraRestoresPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MinimusContainment());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.isArtifact(gd, bears)).isFalse();
        assertThat(gqs.isCreature(gd, bears)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new MinimusContainment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Casting the Aura on a new creature lets its controller use the Treasure ability immediately")
    void newCreatureCanUseTreasureAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        creature.setSummoningSick(true);
        harness.setHand(player1, List.of(new MinimusContainment()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, creature)).isFalse();
        assertThat(gqs.isArtifact(gd, creature)).isTrue();
        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Hill Giant Herdgorger");
        harness.assertInGraveyard(player1, "Minimus Containment");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A noncreature artifact loses its printed abilities and gains only the Treasure ability")
    void artifactLosesPrintedAbilities() {
        Permanent rope = harness.addToBattlefieldAndReturn(player1, new FiftyFeetOfRope());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MinimusContainment());
        aura.setAttachedTo(rope.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Fifty Feet of Rope");
        harness.assertInGraveyard(player1, "Minimus Containment");
        assertThat(gd.stack).isEmpty();
    }
}
