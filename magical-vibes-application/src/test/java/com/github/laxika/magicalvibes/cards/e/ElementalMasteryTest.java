package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.b.BlightSickle;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElementalMastery.class, SafeholdElite.class, BlightSickle.class})
class ElementalMasteryTest extends BaseCardTest {

    private Permanent setupEnchantedCreature() {
        Permanent enchantedCreature = addCreatureReady(player1, new SafeholdElite());

        Permanent auraPerm = new Permanent(new ElementalMastery());
        auraPerm.setAttachedTo(enchantedCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);
        return enchantedCreature;
    }

    private long elementalCount() {
        return countPermanents(player1, "Elemental");
    }

    @Test
    @DisplayName("Granted ability creates Elemental tokens equal to the creature's power")
    void createsTokensEqualToPower() {
        Permanent enchantedCreature = setupEnchantedCreature();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(elementalCount()).isEqualTo(2);
        assertThat(enchantedCreature.isTapped()).isTrue();

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Token count scales with the enchanted creature's effective power")
    void tokenCountScalesWithPower() {
        Permanent enchantedCreature = setupEnchantedCreature();
        enchantedCreature.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE, 3);

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
        setupEnchantedCreature();

        Permanent auraPerm = findPermanent(player1, "Elemental Mastery");
        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new BlightSickle());
        harness.setHand(player1, List.of(new ElementalMastery()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent artifact = findPermanent(player1, "Blight Sickle");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The controller of the enchanted creature controls the granted ability")
    void grantedAbilityUsesEnchantedCreatureController() {
        Permanent enchantedCreature = addCreatureReady(player2, new SafeholdElite());
        Permanent auraPerm = new Permanent(new ElementalMastery());
        auraPerm.setAttachedTo(enchantedCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Elemental")).isEqualTo(2);
        assertThat(countPermanents(player1, "Elemental")).isZero();
    }
}
