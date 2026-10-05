package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DarkProphecy;
import com.github.laxika.magicalvibes.cards.d.DeadlyRecluse;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OathOfTheAncientWood.class, DeadlyRecluse.class, DarkProphecy.class, Naturalize.class, Shock.class})
class OathOfTheAncientWoodTest extends BaseCardTest {

    private void castOath() {
        harness.castFromHand(player1, new OathOfTheAncientWood(), "{2}{G}");
    }

    @Test
    @DisplayName("Its own entry puts a +1/+1 counter on the chosen creature when accepted")
    void selfEntryPutsCounter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DeadlyRecluse());
        castOath();
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Oath of the Ancient Wood");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may leaves the creature without a counter")
    void decliningLeavesNoCounter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DeadlyRecluse());
        castOath();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Another enchantment entering queues a target choice, then puts the counter")
    void allyEnchantmentEntryPutsCounter() {
        harness.addToBattlefield(player1, new OathOfTheAncientWood());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DeadlyRecluse());

        harness.castFromHand(player1, new DarkProphecy(), "{B}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A non-enchantment permanent entering does not trigger the ability")
    void creatureEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new OathOfTheAncientWood());
        harness.castFromHand(player1, new DeadlyRecluse(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The self-entry trigger cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new DarkProphecy());
        harness.addToBattlefield(player1, new DeadlyRecluse());
        castOath();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Oath can enter with no creatures and leaves no targeted trigger or may choice")
    void selfEntryWithNoLegalTargets() {
        castOath();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Oath of the Ancient Wood");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Self-entry can target an opponent's creature that arrived after casting")
    void selfEntryChoosesTargetAfterEntering() {
        castOath();
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new DeadlyRecluse());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's enchantment entering does not trigger Oath")
    void opponentEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new OathOfTheAncientWood());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DeadlyRecluse());
        harness.enterBattlefieldAndReturn(player2, new DarkProphecy());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining another enchantment's entry trigger leaves no counter")
    void decliningAllyEntryLeavesNoCounter() {
        harness.addToBattlefield(player1, new OathOfTheAncientWood());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DeadlyRecluse());
        harness.castFromHand(player1, new DarkProphecy(), "{B}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Another enchantment entering without creatures leaves no target choice")
    void allyEntryWithNoLegalTargets() {
        harness.addToBattlefield(player1, new OathOfTheAncientWood());
        harness.castFromHand(player1, new DarkProphecy(), "{B}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dark Prophecy");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The entry trigger resolves even after Oath is destroyed")
    void triggerSurvivesSourceRemoval() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DeadlyRecluse());
        castOath();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Oath of the Ancient Wood"));
        harness.assertInGraveyard(player1, "Oath of the Ancient Wood");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A removed target makes the trigger fail without offering the may choice")
    void triggerDoesNotRetargetAfterTargetDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DeadlyRecluse());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new DeadlyRecluse());
        castOath();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.passBothPriorities();

        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Oath of the Ancient Wood");
    }
}
