package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinSledder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishGuidance.class, Forest.class, ElvishPioneer.class, GoblinSledder.class})
class ElvishGuidanceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Elvish Guidance attaches it to target land")
    void resolvingAttachesToTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ElvishGuidance()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Elvish Guidance")
                        && forest.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Tapping enchanted land adds one green mana for each Elf on the battlefield")
    void enchantedLandAddsManaForEachElfOnBattlefield() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new ElvishPioneer());
        harness.addToBattlefield(player2, new ElvishPioneer());
        harness.addToBattlefield(player1, new GoblinSledder());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ElvishGuidance());
        aura.setAttachedTo(forest.getId());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Tapping enchanted land adds no bonus when there are no Elves")
    void enchantedLandAddsNoBonusWithoutElves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ElvishGuidance());
        aura.setAttachedTo(forest.getId());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping a different land does not get Elvish Guidance's bonus")
    void differentLandDoesNotGetBonus() {
        Permanent enchantedForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent otherForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new ElvishPioneer());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ElvishGuidance());
        aura.setAttachedTo(enchantedForest.getId());

        harness.tapPermanent(player1, 1);

        assertThat(otherForest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Controller of enchanted land gets the Elf-based bonus")
    void enchantedLandControllerGetsBonus() {
        harness.addToBattlefield(player1, new ElvishPioneer());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ElvishGuidance());
        aura.setAttachedTo(forest.getId());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Cannot cast Elvish Guidance targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GoblinSledder());
        Permanent goblin = findPermanent(player1, "Goblin Sledder");
        harness.setHand(player1, List.of(new ElvishGuidance()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, goblin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
