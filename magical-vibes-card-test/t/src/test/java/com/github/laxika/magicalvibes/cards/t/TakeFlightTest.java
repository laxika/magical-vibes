package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({TakeFlight.class, GrizzlyBears.class, FountainOfYouth.class})
class TakeFlightTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+0 and flying")
    void boostsAndGrantsFlying() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Attacking with the enchanted creature draws a card for the creature's controller")
    void attackingDrawsCardForCreatureController() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachAura(creature);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player2.getId()).size();
        int auraHandBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(auraHandBefore);
    }

    @Test
    @DisplayName("Take Flight can target only a creature")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new TakeFlight()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Casting Take Flight attaches it and grants its effects")
    void castingAttachesAndGrantsEffects() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TakeFlight()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Take Flight").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Attacking with another creature does not draw a card")
    void unrelatedAttackerDoesNotDraw() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        attachAura(enchanted);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TakeFlight());
        aura.setAttachedTo(creature.getId());
    }
}
