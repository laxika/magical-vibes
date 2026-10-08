package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VerdantEmbrace.class, AshcoatBear.class, PrismaticLens.class})
class VerdantEmbraceTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+3")
    void enchantedCreatureGetsBoost() {
        Permanent bears = addCreatureReady(player1, new AshcoatBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VerdantEmbrace());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Creates a 1/1 green Saproling during each upkeep")
    void createsSaprolingDuringEachUpkeep() {
        Permanent bears = addCreatureReady(player1, new AshcoatBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VerdantEmbrace());
        aura.setAttachedTo(bears.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Saproling");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        });
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PrismaticLens());
        harness.setHand(player1, List.of(new VerdantEmbrace()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted opponent's creature creates tokens for its controller each upkeep")
    void opponentControlsGrantedUpkeepAbility() {
        Permanent creature = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new VerdantEmbrace()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(countPermanents(player2, "Saproling")).isEqualTo(1);
        assertThat(countPermanents(player1, "Saproling")).isZero();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(countPermanents(player2, "Saproling")).isEqualTo(2);
        assertThat(countPermanents(player1, "Saproling")).isZero();
    }

    @Test
    @DisplayName("Two Embraces grant two upkeep abilities and stack their boosts")
    void multipleEmbracesGrantSeparateAbilities() {
        Permanent creature = addCreatureReady(player1, new AshcoatBear());
        Permanent firstAura = harness.addToBattlefieldAndReturn(player1, new VerdantEmbrace());
        firstAura.setAttachedTo(creature.getId());
        Permanent secondAura = harness.addToBattlefieldAndReturn(player1, new VerdantEmbrace());
        secondAura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Saproling")).isEqualTo(2);
    }
}
