package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.a.AngelicShield;
import com.github.laxika.magicalvibes.cards.h.HammerOfPurphoros;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferisCare.class, AngelicShield.class, AngelOfMercy.class})
class TeferisCareTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target enchantment, sacrificing itself to pay the cost")
    void destroysTargetEnchantment() {
        harness.addToBattlefield(player1, new TeferisCare());
        harness.addToBattlefield(player2, new AngelicShield());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Shield");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Angelic Shield");
        harness.assertInGraveyard(player2, "Angelic Shield");
        harness.assertInGraveyard(player1, "Teferi's Care");
    }

    @Test
    @DisplayName("Can sacrifice a different enchantment to pay the destroy ability")
    void canSacrificeDifferentEnchantment() {
        harness.addToBattlefield(player1, new TeferisCare());
        Permanent sacrificedEnchantment = harness.addToBattlefieldAndReturn(player1, new AngelicShield());
        harness.addToBattlefield(player2, new AngelicShield());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Shield");
        harness.activateAbility(player1, 0, 0, null, targetId);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrificedEnchantment.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Teferi's Care");
        harness.assertInGraveyard(player1, "Angelic Shield");
        harness.assertNotOnBattlefield(player2, "Angelic Shield");
    }

    @Test
    @DisplayName("Destroy ability cannot target a creature")
    void destroyCannotTargetCreature() {
        harness.addToBattlefield(player1, new TeferisCare());
        harness.addToBattlefield(player2, new AngelOfMercy());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID angelId = harness.getPermanentId(player2, "Angel of Mercy");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, angelId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a target enchantment spell")
    void countersEnchantmentSpell() {
        harness.addToBattlefield(player1, new TeferisCare());
        harness.addMana(player1, ManaColor.BLUE, 5);

        AngelicShield shield = new AngelicShield();
        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, shield, "{W}{U}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, 1, null, shield.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Angelic Shield");
        harness.assertInGraveyard(player2, "Angelic Shield");
    }

    @Test
    @CardUsed(HammerOfPurphoros.class)
    @DisplayName("Counter ability can target an artifact-enchantment spell")
    void countersArtifactEnchantmentSpell() {
        harness.addToBattlefield(player1, new TeferisCare());
        harness.addMana(player1, ManaColor.BLUE, 5);

        HammerOfPurphoros spell = new HammerOfPurphoros();
        spell.setType(CardType.ARTIFACT);
        spell.setAdditionalTypes(Set.of(CardType.ENCHANTMENT));
        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, spell, "{1}{R}{R}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, 1, null, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hammer of Purphoros");
    }

    @Test
    @DisplayName("Counter ability requires two blue mana")
    void counterRequiresTwoBlueMana() {
        harness.addToBattlefield(player1, new TeferisCare());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        AngelicShield shield = new AngelicShield();
        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, shield, "{W}{U}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, shield.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Counter ability cannot target a creature spell")
    void counterCannotTargetCreatureSpell() {
        harness.addToBattlefield(player1, new TeferisCare());
        harness.addMana(player1, ManaColor.BLUE, 5);

        AngelOfMercy angel = new AngelOfMercy();
        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, angel, "{4}{W}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, angel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
