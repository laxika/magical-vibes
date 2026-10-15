package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NewHorizons.class, Forest.class, JungleDelver.class})
class NewHorizonsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting New Horizons targets only the land")
    void castingPutsOnStack() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new JungleDelver());
        harness.setHand(player1, List.of(new NewHorizons()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(forest.getId());
        assertThat(entry.getTargetIds()).isEmpty();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Resolving New Horizons attaches to land and puts +1/+1 counter on creature")
    void resolvingAttachesAndPutsCounter() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new JungleDelver());
        harness.setHand(player1, List.of(new NewHorizons()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        // Resolve the aura spell — aura attaches to land, ETB trigger goes on stack
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        // Resolve the ETB trigger — put +1/+1 counter on creature
        harness.passBothPriorities();

        // Aura is attached to forest
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("New Horizons")
                        && forest.getId().equals(p.getAttachedTo()));
        // Creature has +1/+1 counter
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enchanted land gains mana ability that produces two mana of any one color")
    void enchantedLandGainsManaAbility() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new NewHorizons());
        aura.setAttachedTo(forest.getId());

        // Activate the granted mana ability on the forest (ability index 0)
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        // Forest should be tapped after activating the ability
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted land can still produce its normal mana when tapped directly")
    void enchantedLandStillProducesNormalMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new NewHorizons());
        aura.setAttachedTo(forest.getId());

        // Tap the forest directly for normal mana (not using the granted ability)
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Granted mana ability goes away when aura leaves battlefield")
    void manaAbilityRemovedWhenAuraLeaves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new NewHorizons());
        aura.setAttachedTo(forest.getId());

        // Remove the aura
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        // Forest should have no granted activated abilities
        var staticBonus = gqs.computeStaticBonus(gd, forest);
        assertThat(staticBonus.grantedActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("New Horizons resolves without any creatures and still grants mana")
    void resolvesWithoutCreatures() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new JungleDelver());
        harness.setHand(player1, List.of(new NewHorizons()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "New Horizons");
        assertThat(gd.stack).isEmpty();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The opponent's enchanted land produces mana for its controller")
    void canEnchantOpponentsLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JungleDelver());
        harness.setHand(player1, List.of(new NewHorizons()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "WHITE");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An illegal land target prevents entry and the counter trigger")
    void missingLandPreventsEntry() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JungleDelver());
        harness.setHand(player1, List.of(new NewHorizons()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        gd.playerBattlefields.get(player1.getId()).remove(forest);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "New Horizons");
        harness.assertInGraveyard(player1, "New Horizons");
        assertThat(gd.stack).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter can target a creature that entered after the Aura was cast")
    void choosesCreatureAfterAuraEnters() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new NewHorizons()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, forest.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JungleDelver());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter trigger resolves even after New Horizons leaves")
    void counterTriggerSurvivesAuraLeaving() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JungleDelver());
        harness.setHand(player1, List.of(new NewHorizons()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof NewHorizons);

        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
