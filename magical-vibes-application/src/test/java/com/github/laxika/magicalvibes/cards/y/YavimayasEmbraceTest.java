package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.v.Vindicate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YavimayasEmbrace.class, Dodecapod.class, Vindicate.class, YavimayaCoast.class})
class YavimayasEmbraceTest extends BaseCardTest {

    @Test
    @DisplayName("Yavimaya's Embrace steals the enchanted creature and grants its bonuses")
    void stealsAndBoostsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new Dodecapod());

        castAuraOn(creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Destroying Yavimaya's Embrace returns the creature and removes its bonuses")
    void removingAuraReturnsCreatureAndRemovesBonuses() {
        Permanent creature = addCreatureReady(player2, new Dodecapod());

        castAuraOn(creature);
        Permanent aura = findPermanent(player1, "Yavimaya's Embrace");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Vindicate()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player2, 0, aura.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Yavimaya's Embrace cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new YavimayaCoast());
        harness.setHand(player1, List.of(new YavimayasEmbrace()));
        addEmbraceMana(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanting your own creature grants bonuses without making it summoning sick")
    void canEnchantOwnCreature() {
        Permanent creature = addCreatureReady(player1, new Dodecapod());

        castAuraOn(creature);

        harness.assertOnBattlefield(player1, "Dodecapod");
        assertThat(creature.isSummoningSick()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The newest Embrace controls the creature and removing it restores the older Embrace")
    void overlappingEmbracesUseNewestControlAndStackBonuses() {
        Permanent creature = addCreatureReady(player2, new Dodecapod());
        castAuraOn(creature);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new YavimayasEmbrace()));
        addEmbraceMana(player2);
        harness.castEnchantment(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dodecapod");
        harness.assertNotOnBattlefield(player1, "Dodecapod");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        Permanent newestAura = findPermanent(player2, "Yavimaya's Embrace");
        harness.setHand(player2, List.of(new Vindicate()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player2, 0, newestAura.getId());

        harness.assertOnBattlefield(player1, "Dodecapod");
        harness.assertNotOnBattlefield(player2, "Dodecapod");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Destroying the enchanted creature sends it to its owner's graveyard and removes the Aura")
    void enchantedCreatureDiesToOwnersGraveyard() {
        Permanent creature = addCreatureReady(player2, new Dodecapod());
        castAuraOn(creature);

        harness.setHand(player1, List.of(new Vindicate()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertInGraveyard(player2, "Dodecapod");
        harness.assertNotInGraveyard(player1, "Dodecapod");
        harness.assertInGraveyard(player1, "Yavimaya's Embrace");
        harness.assertNotOnBattlefield(player1, "Dodecapod");
        harness.assertNotOnBattlefield(player1, "Yavimaya's Embrace");
        harness.assertNotOnBattlefield(player2, "Dodecapod");
    }

    private void castAuraOn(Permanent target) {
        harness.setHand(player1, List.of(new YavimayasEmbrace()));
        addEmbraceMana(player1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addEmbraceMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 5);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.BLUE, 2);
    }
}
