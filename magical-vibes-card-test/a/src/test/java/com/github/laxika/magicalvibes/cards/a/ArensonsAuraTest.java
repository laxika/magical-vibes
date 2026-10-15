package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HammerOfPurphoros;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArensonsAura.class, BadMoon.class, GrizzlyBears.class, HammerOfPurphoros.class})
class ArensonsAuraTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target enchantment, sacrificing itself to pay the cost")
    void destroysTargetEnchantment() {
        harness.addToBattlefield(player1, new ArensonsAura());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BadMoon()).getId();
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Only enchantment player1 controls is Arenson's Aura → auto-sacrificed
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Bad Moon");
        harness.assertInGraveyard(player2, "Bad Moon");
        // Aura sacrificed to pay the cost
        harness.assertInGraveyard(player1, "Arenson's Aura");
    }

    @Test
    @DisplayName("Can sacrifice another enchantment to destroy the target")
    void sacrificesAnotherEnchantment() {
        harness.addToBattlefield(player1, new ArensonsAura());
        harness.addToBattlefield(player1, new BadMoon());
        harness.addToBattlefield(player2, new BadMoon());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID sacrificedId = harness.getPermanentId(player1, "Bad Moon");
        UUID targetId = harness.getPermanentId(player2, "Bad Moon");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.handlePermanentChosen(player1, sacrificedId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Arenson's Aura");
        harness.assertInGraveyard(player1, "Bad Moon");
        harness.assertInGraveyard(player2, "Bad Moon");
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, before destruction resolves")
    void sacrificeIsPaidBeforeResolution() {
        harness.addToBattlefield(player1, new ArensonsAura());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BadMoon()).getId();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, targetId);

        harness.assertInGraveyard(player1, "Arenson's Aura");
        harness.assertNotOnBattlefield(player1, "Arenson's Aura");
        harness.assertOnBattlefield(player2, "Bad Moon");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Bad Moon");
    }

    @Test
    @DisplayName("Can target itself and sacrifice itself, leaving no legal target")
    void canTargetAndSacrificeItself() {
        UUID auraId = harness.addToBattlefieldAndReturn(player1, new ArensonsAura()).getId();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, auraId);
        harness.assertInGraveyard(player1, "Arenson's Aura");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Arenson's Aura");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can destroy another enchantment controlled by its controller")
    void destroysOwnEnchantment() {
        harness.addToBattlefield(player1, new ArensonsAura());
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new BadMoon()).getId();
        UUID auraId = harness.getPermanentId(player1, "Arenson's Aura");
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.handlePermanentChosen(player1, auraId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Arenson's Aura");
        harness.assertInGraveyard(player1, "Bad Moon");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Destroy ability requires white mana and does not sacrifice on failed activation")
    void destroyRequiresWhiteMana() {
        harness.addToBattlefield(player1, new ArensonsAura());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BadMoon()).getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Arenson's Aura");
        harness.assertOnBattlefield(player2, "Bad Moon");
        harness.assertNotInGraveyard(player1, "Arenson's Aura");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Destroy ability cannot target a creature")
    void destroyCannotTargetCreature() {
        harness.addToBattlefield(player1, new ArensonsAura());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bearId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a target enchantment spell")
    void countersEnchantmentSpell() {
        harness.addToBattlefield(player1, new ArensonsAura());
        harness.addMana(player1, ManaColor.BLUE, 5);

        BadMoon badMoon = new BadMoon();
        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, badMoon, "{1}{B}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, 1, null, badMoon.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Bad Moon");
        harness.assertInGraveyard(player2, "Bad Moon");
    }

    @Test
    @DisplayName("Counter ability cannot target a creature spell")
    void counterCannotTargetCreatureSpell() {
        harness.addToBattlefield(player1, new ArensonsAura());
        harness.addMana(player1, ManaColor.BLUE, 5);

        GrizzlyBears bears = new GrizzlyBears();
        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, bears, "{1}{G}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counter ability requires two blue mana")
    void counterRequiresTwoBlueMana() {
        harness.addToBattlefield(player1, new ArensonsAura());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        BadMoon badMoon = new BadMoon();
        harness.forceActivePlayer(player2);
        harness.forceStep(harness.getGameData().currentStep);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, badMoon, "{1}{B}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, badMoon.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @CardUsed(HammerOfPurphoros.class)
    @DisplayName("Counter ability can target an artifact-enchantment spell")
    void countersArtifactEnchantmentSpell() {
        harness.addToBattlefield(player1, new ArensonsAura());
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
}
