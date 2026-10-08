package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DeadlyRecluse;
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

@CardUsed({VerdantHaven.class, Forest.class, DeadlyRecluse.class})
class VerdantHavenTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Verdant Haven attaches it to target land and gains 2 life")
    void resolvingAttachesAndGainsLife() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.setHand(player1, List.of(new VerdantHaven()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Verdant Haven")
                        && forest.getId().equals(p.getAttachedTo()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("Tapping enchanted land adds one extra mana of the chosen color")
    void enchantedLandAddsExtraManaOfChosenColor() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VerdantHaven());
        aura.setAttachedTo(forest.getId());

        harness.tapPermanent(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Controller of enchanted land gets bonus mana even if aura is controlled by opponent")
    void enchantedLandControllerGetsBonus() {
        Permanent opponentsForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VerdantHaven());
        aura.setAttachedTo(opponentsForest.getId());

        harness.tapPermanent(player2, 0);
        harness.handleListChoice(player2, "RED");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast Verdant Haven targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DeadlyRecluse());
        harness.setHand(player1, List.of(new VerdantHaven()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Enchanting an opponent's land gains life for the Aura controller")
    void enchantingOpponentsLandGainsLifeForAuraController() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new VerdantHaven()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 10);
        assertThat(findPermanent(player1, "Verdant Haven").getAttachedTo()).isEqualTo(land.getId());
    }

    @Test
    @DisplayName("Tapping a different land does not produce bonus mana")
    void unrelatedLandDoesNotProduceBonusMana() {
        Permanent enchantedLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VerdantHaven());
        aura.setAttachedTo(enchantedLand.getId());

        harness.tapPermanent(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two Verdant Havens each produce one independently chosen bonus mana")
    void multipleAurasEachProduceBonusMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent firstAura = harness.addToBattlefieldAndReturn(player1, new VerdantHaven());
        firstAura.setAttachedTo(land.getId());
        Permanent secondAura = harness.addToBattlefieldAndReturn(player1, new VerdantHaven());
        secondAura.setAttachedTo(land.getId());

        harness.tapPermanent(player1, 0);
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
