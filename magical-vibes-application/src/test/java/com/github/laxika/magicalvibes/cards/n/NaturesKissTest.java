package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NaturesKissTest extends BaseCardTest {

    private Permanent attachKissTo(Permanent host) {
        Permanent auraPerm = new Permanent(new NaturesKiss());
        auraPerm.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);
        return auraPerm;
    }

    @Test
    @DisplayName("Activating gives the enchanted creature +1/+1 and exiles the top graveyard card")
    void activatedAbilityBoostsAndExiles() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachKissTo(bears);
        harness.setGraveyard(player1, List.of(new Plains(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Plains");
        assertThat(gd.exiledCards)
                .extracting(e -> e.card().getName())
                .contains("Forest");
    }

    @Test
    @DisplayName("Boost stacks across multiple activations")
    void boostStacks() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachKissTo(bears);
        harness.setGraveyard(player1, List.of(new Plains(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachKissTo(bears);
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot be activated with an empty graveyard")
    void requiresNonEmptyGraveyard() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachKissTo(bears);
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new NaturesKiss()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
