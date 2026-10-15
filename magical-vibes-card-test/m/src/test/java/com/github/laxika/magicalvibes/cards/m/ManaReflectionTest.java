package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FireLitThicket;
import com.github.laxika.magicalvibes.cards.d.DevotedDruid;
import com.github.laxika.magicalvibes.cards.d.DampingSphere;
import com.github.laxika.magicalvibes.cards.g.GeneratorServant;
import com.github.laxika.magicalvibes.cards.s.SapseepForest;
import com.github.laxika.magicalvibes.cards.s.SkirkProspector;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaReflection.class, Forest.class, DevotedDruid.class, SapseepForest.class,
        SkirkProspector.class, FireLitThicket.class, DampingSphere.class, GeneratorServant.class})
class ManaReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("Without Mana Reflection a Forest taps for one green mana")
    void baselineSingleMana() {
        harness.addToBattlefield(player1, new Forest());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("With Mana Reflection tapping a permanent for mana produces twice as much")
    void doublesProducedMana() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new ManaReflection());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Mana Reflections stack multiplicatively (quadruple)")
    void stacksMultiplicatively() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new ManaReflection());
        harness.addToBattlefield(player1, new ManaReflection());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    @Test
    @DisplayName("Mana Reflection does not double an opponent's mana")
    void manaReflectionOnlyAffectsItsController() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new ManaReflection());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mana Reflection doubles mana produced by a creature")
    void doublesCreatureMana() {
        addCreatureReady(player1, new DevotedDruid());
        harness.addToBattlefield(player1, new ManaReflection());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mana Reflection doubles an activated land mana ability")
    void doublesActivatedLandMana() {
        harness.addToBattlefield(player1, new SapseepForest());
        harness.addToBattlefield(player1, new ManaReflection());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana Reflection does not multiply mana from an ability without a tap cost")
    void doesNotDoubleSacrificeOnlyManaAbility() {
        harness.addToBattlefield(player1, new SkirkProspector());
        harness.addToBattlefield(player1, new ManaReflection());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Skirk Prospector");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana Reflection doubles each color produced without adding independent color choices")
    void doublesChosenColorCombination() {
        harness.addToBattlefield(player1, new FireLitThicket());
        harness.addToBattlefield(player1, new ManaReflection());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damping Sphere replaces a Forest's doubled mana with one colorless mana")
    void dampingSphereAppliesAfterForestManaIsDoubled() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new ManaReflection());
        harness.addToBattlefield(player2, new DampingSphere());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Damping Sphere replaces doubled mana from an activated land mana ability")
    void dampingSphereAppliesAfterActivatedLandManaIsDoubled() {
        harness.addToBattlefield(player1, new SapseepForest());
        harness.addToBattlefield(player1, new ManaReflection());
        harness.addToBattlefield(player2, new DampingSphere());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mana Reflection doubles haste-granting mana and preserves its rider")
    void doublesHasteGrantingMana() {
        addCreatureReady(player1, new GeneratorServant());
        harness.addToBattlefield(player1, new ManaReflection());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getHasteGrantingMana(ManaColor.COLORLESS))
                .isEqualTo(4);
        harness.assertInGraveyard(player1, "Generator Servant");
        assertThat(gd.stack).isEmpty();
    }
}
