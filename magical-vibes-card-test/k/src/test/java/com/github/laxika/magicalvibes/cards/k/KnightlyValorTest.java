package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AzoriusKeyrune;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightlyValor.class, DrudgeBeetle.class, AzoriusKeyrune.class})
class KnightlyValorTest extends BaseCardTest {

    private Permanent castOn(Permanent creature) {
        harness.setHand(player1, List.of(new KnightlyValor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        return findPermanent(player1, "Knightly Valor");
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and vigilance")
    void enchantedCreatureBoostedAndVigilant() {
        Permanent bears = addCreatureReady(player1, new DrudgeBeetle());
        castOn(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Entering creates a 2/2 white Knight token with vigilance")
    void entersCreatingKnightToken() {
        Permanent bears = addCreatureReady(player1, new DrudgeBeetle());
        castOn(bears);
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Knight");
        assertThat(countPermanents(player1, "Knight")).isEqualTo(1);
        assertThat(knight.getCard().isToken()).isTrue();
        assertThat(knight.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(knight.getCard().getSubtypes()).containsExactly(CardSubtype.KNIGHT);
        assertThat(knight.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Boost and vigilance go away when the Aura leaves the battlefield")
    void boostStopsWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new DrudgeBeetle());
        Permanent aura = castOn(bears);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player2, new DrudgeBeetle());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AzoriusKeyrune());
        harness.setHand(player1, List.of(new KnightlyValor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanting an opponent's creature creates the Knight for the Aura controller")
    void opponentCreatureGetsBoostButNotToken() {
        Permanent creature = addCreatureReady(player2, new DrudgeBeetle());
        Permanent aura = castOn(creature);
        resolveAllTriggers();

        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(countPermanents(player1, "Knight")).isEqualTo(1);
        assertThat(countPermanents(player2, "Knight")).isZero();
    }

    @Test
    @DisplayName("The Knight trigger resolves even after the Aura leaves")
    void tokenTriggerSurvivesAuraRemoval() {
        Permanent creature = addCreatureReady(player1, new DrudgeBeetle());
        Permanent aura = castOn(creature);
        assertThat(countPermanents(player1, "Knight")).isZero();

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Knight")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("An Aura with a missing target does not enter or create a Knight")
    void missingTargetPreventsEntryAndToken() {
        Permanent creature = addCreatureReady(player1, new DrudgeBeetle());
        harness.setHand(player1, List.of(new KnightlyValor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Knightly Valor");
        harness.assertInGraveyard(player1, "Knightly Valor");
        assertThat(countPermanents(player1, "Knight")).isZero();
    }

    @Test
    @DisplayName("Vigilance lets the enchanted creature attack without tapping")
    void enchantedCreatureAttacksWithoutTapping() {
        Permanent creature = addCreatureReady(player1, new DrudgeBeetle());
        castOn(creature);
        resolveAllTriggers();

        declareAttackers(List.of(0));

        assertThat(creature.isTapped()).isFalse();
    }
}
