package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElementalMasteryTest extends BaseCardTest {

    private Permanent setupEnchantedCreature() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent auraPerm = new Permanent(new ElementalMastery());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);
        return bearsPerm;
    }

    private long elementalCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elemental"))
                .count();
    }

    @Test
    @DisplayName("Granted ability creates Elemental tokens equal to the creature's power")
    void createsTokensEqualToPower() {
        Permanent bearsPerm = setupEnchantedCreature();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Grizzly Bears is 2/2 -> two 1/1 Elemental tokens
        assertThat(elementalCount()).isEqualTo(2);
        assertThat(bearsPerm.isTapped()).isTrue();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elemental"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Token count scales with the enchanted creature's effective power")
    void tokenCountScalesWithPower() {
        Permanent bearsPerm = setupEnchantedCreature();
        bearsPerm.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE, 3); // 2/2 -> 5/5

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(elementalCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("Elemental tokens are exiled at the beginning of the next end step")
    void tokensExiledAtEndStep() {
        setupEnchantedCreature();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(elementalCount()).isEqualTo(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elementalCount()).isZero();
    }

    @Test
    @DisplayName("Creature loses the granted ability when Elemental Mastery is removed")
    void abilityLostWhenRemoved() {
        Permanent bearsPerm = setupEnchantedCreature();

        Permanent auraPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elemental Mastery"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ElementalMastery()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
