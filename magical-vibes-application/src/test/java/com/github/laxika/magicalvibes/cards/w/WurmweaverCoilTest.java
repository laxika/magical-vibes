package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DaggerclawImp;
import com.github.laxika.magicalvibes.cards.g.GruulNodorog;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({WurmweaverCoil.class, GruulNodorog.class, DaggerclawImp.class})
class WurmweaverCoilTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Wurmweaver Coil attaches it and gives a green creature +6/+6")
    void resolvingAttachesAndBoostsGreenCreature() {
        Permanent creature = addCreatureReady(player1, new GruulNodorog());
        harness.setHand(player1, List.of(new WurmweaverCoil()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(10);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof WurmweaverCoil
                        && permanent.isAttached()
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Wurmweaver Coil does not boost another creature")
    void doesNotBoostAnotherCreature() {
        Permanent creature = addCreatureReady(player1, new GruulNodorog());
        Permanent otherCreature = addCreatureReady(player1, new GruulNodorog());

        Permanent coil = harness.addToBattlefieldAndReturn(player1, new WurmweaverCoil());
        coil.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(10);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Wurmweaver Coil cannot enchant a non-green creature")
    void cannotEnchantNonGreenCreature() {
        Permanent creature = addCreatureReady(player1, new DaggerclawImp());
        harness.setHand(player1, List.of(new WurmweaverCoil()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a green creature");
    }

    @Test
    @DisplayName("Wurmweaver Coil cannot enchant a green noncreature")
    void cannotEnchantGreenNonCreature() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new WurmweaverCoil());
        harness.setHand(player1, List.of(new WurmweaverCoil()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a green creature");
    }

    @Test
    @DisplayName("Sacrificing Wurmweaver Coil creates a 6/6 green Wurm token")
    void sacrificingCreatesWurmToken() {
        Permanent creature = addCreatureReady(player1, new GruulNodorog());
        Permanent coil = harness.addToBattlefieldAndReturn(player1, new WurmweaverCoil());
        coil.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Wurm");
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.WURM);
                    assertThat(token.getEffectivePower()).isEqualTo(6);
                    assertThat(token.getEffectiveToughness()).isEqualTo(6);
                });
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Wurmweaver Coil");
    }
    @Test
    @DisplayName("Coil can enchant an opponent's creature but its controller receives the Wurm")
    void enchantingOpponentsCreatureCreatesTokenForAuraController() {
        Permanent creature = addCreatureReady(player2, new GruulNodorog());
        harness.setHand(player1, List.of(new WurmweaverCoil()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(10);
        assertThat(findPermanent(player1, "Wurmweaver Coil").getAttachedTo()).isEqualTo(creature.getId());

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wurm");
        harness.assertNotOnBattlefield(player2, "Wurm");
        harness.assertOnBattlefield(player2, "Gruul Nodorog");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sacrifice removes Coil and its boost before the Wurm ability resolves")
    void sacrificeIsPaidBeforeTokenCreation() {
        Permanent creature = addCreatureReady(player1, new GruulNodorog());
        Permanent coil = harness.addToBattlefieldAndReturn(player1, new WurmweaverCoil());
        coil.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Wurmweaver Coil");
        harness.assertInGraveyard(player1, "Wurmweaver Coil");
        harness.assertNotOnBattlefield(player1, "Wurm");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wurm");
    }

    @Test
    @DisplayName("Coil cannot be sacrificed without paying three green mana")
    void insufficientGreenManaDoesNotSacrificeAura() {
        Permanent creature = addCreatureReady(player1, new GruulNodorog());
        Permanent coil = harness.addToBattlefieldAndReturn(player1, new WurmweaverCoil());
        coil.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Wurmweaver Coil");
        harness.assertNotInGraveyard(player1, "Wurmweaver Coil");
        harness.assertNotOnBattlefield(player1, "Wurm");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(10);
        assertThat(gd.stack).isEmpty();
    }
}
