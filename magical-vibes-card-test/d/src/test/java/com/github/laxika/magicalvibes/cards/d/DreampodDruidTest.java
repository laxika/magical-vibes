package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
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

@CardUsed({DreampodDruid.class, Pacifism.class, HolyStrength.class, Naturalize.class, SwordsToPlowshares.class})
class DreampodDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Does not create a Saproling while it is not enchanted")
    void doesNotCreateTokenWhenNotEnchanted() {
        harness.addToBattlefield(player1, new DreampodDruid());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Saproling")).isZero();
    }

    @Test
    @DisplayName("Creates a Saproling during each upkeep while enchanted")
    void createsTokenDuringEachUpkeepWhenEnchanted() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new DreampodDruid());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(druid.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Saproling")).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates a Saproling during each player's upkeep while enchanted")
    void createsSaprolingDuringEachUpkeepWhileEnchanted() {
        harness.addToBattlefield(player1, new DreampodDruid());
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, harness.getPermanentId(player1, "Dreampod Druid"));
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling"))
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
                });

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
    }

    @Test
    @DisplayName("Does not create a Saproling when it is not enchanted")
    void doesNotCreateSaprolingWhenNotEnchanted() {
        harness.addToBattlefield(player1, new DreampodDruid());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Aura still creates a token for the Druid's controller")
    void opponentsAuraCreatesTokenForDruidController() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new DreampodDruid());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        aura.setAttachedTo(druid.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Removing the only Aura in response stops token creation")
    void removingOnlyAuraBeforeResolutionStopsTokenCreation() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new DreampodDruid());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(druid.getId());
        harness.setHand(player1, List.of(new Naturalize()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.assertInGraveyard(player1, "Pacifism");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Multiple Auras produce only one token and losing one Aura does not stop it")
    void remainingAuraAllowsSingleToken() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new DreampodDruid());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(druid.getId());
        Permanent otherAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        otherAura.setAttachedTo(druid.getId());
        harness.setHand(player1, List.of(new Naturalize()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("Uses last known enchanted status when the Druid leaves before resolution")
    void createsTokenWhenEnchantedDruidLeavesBeforeResolution() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new DreampodDruid());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(druid.getId());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, druid.getId());
        harness.assertNotOnBattlefield(player1, "Dreampod Druid");
        harness.assertInGraveyard(player1, "Pacifism");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }
}
