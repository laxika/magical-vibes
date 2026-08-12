package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrandOfIllOmenTest extends BaseCardTest {

    private Permanent attachToOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new BrandOfIllOmen()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        return creature;
    }

    @Test
    @DisplayName("Enchanted creature's controller cannot cast creature spells")
    void enchantedControllerCannotCastCreatureSpells() {
        attachToOpponentCreature();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Aura's controller can still cast creature spells")
    void auraControllerCanStillCastCreatureSpells() {
        attachToOpponentCreature();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Enchanted creature's controller can cast noncreature spells")
    void enchantedControllerCanCastNoncreatureSpells() {
        attachToOpponentCreature();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Restriction ends when the Aura leaves the battlefield")
    void restrictionEndsWhenAuraLeaves() {
        attachToOpponentCreature();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> "Brand of Ill Omen".equals(p.getCard().getName()));

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.f.FountainOfYouth());
        java.util.UUID artifactId = harness.getPermanentId(player2, "Fountain of Youth");

        harness.setHand(player1, List.of(new BrandOfIllOmen()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifactId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cumulative upkeep adds an age counter and keeps the Aura when paid")
    void cumulativeUpkeepPaid() {
        Permanent creature = attachToOpponentCreature();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Brand of Ill Omen".equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(aura.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Cumulative upkeep sacrifices the Aura when unpaid")
    void cumulativeUpkeepUnpaidSacrificesAura() {
        attachToOpponentCreature();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Brand of Ill Omen");
        harness.assertInGraveyard(player1, "Brand of Ill Omen");
    }
}
