package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.u.UrzasBauble;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrandOfIllOmen.class, BalduvianBears.class, DarkRitual.class, UrzasBauble.class})
class BrandOfIllOmenTest extends BaseCardTest {

    private Permanent attachToOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new BalduvianBears());

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

        harness.setHand(player2, List.of(new BalduvianBears()));
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

        harness.setHand(player1, List.of(new BalduvianBears()));
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

        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Restriction ends when the Aura leaves the battlefield")
    void restrictionEndsWhenAuraLeaves() {
        attachToOpponentCreature();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> "Brand of Ill Omen".equals(p.getCard().getName()));

        harness.setHand(player2, List.of(new BalduvianBears()));
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new UrzasBauble());

        harness.setHand(player1, List.of(new BrandOfIllOmen()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cumulative upkeep adds an age counter and keeps the Aura when paid")
    void cumulativeUpkeepPaid() {
        Permanent creature = attachToOpponentCreature();
        Permanent aura = findPermanent(player1, "Brand of Ill Omen");

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
    @DisplayName("Cumulative upkeep costs one red mana per age counter")
    void cumulativeUpkeepScalesWithAgeCounters() {
        attachToOpponentCreature();
        Permanent aura = findPermanent(player1, "Brand of Ill Omen");
        aura.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(aura.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
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
