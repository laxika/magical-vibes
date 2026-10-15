package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InspiringCleric;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.s.ShepherdOfHeroes;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThunderingSparkmage.class, BoggartBrute.class, ChandraNalaar.class,
        FaerieMiscreant.class, FugitiveWizard.class, GrizzlyBears.class,
        InspiringCleric.class, Island.class, IntoTheRoil.class, ShepherdOfHeroes.class,
        TajuruParagon.class})
class ThunderingSparkmageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the controller's party size")
    void dealsDamageEqualToPartySize() {
        harness.addToBattlefield(player1, new InspiringCleric());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());

        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        castSparkmage(planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts only party creatures controlled by the Sparkmage's controller")
    void countsOnlyControllersParty() {
        harness.addToBattlefield(player2, new InspiringCleric());
        harness.addToBattlefield(player2, new FaerieMiscreant());
        harness.addToBattlefield(player2, new BoggartBrute());
        harness.addToBattlefield(player2, new FugitiveWizard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSparkmage(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ThunderingSparkmage()));
        addSparkmageMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or planeswalker");
    }

    @Test
    @DisplayName("Multiple Wizards fill only one party role")
    void duplicateWizardsDoNotIncreaseDamage() {
        harness.addToBattlefield(player1, new ThunderingSparkmage());
        harness.addToBattlefield(player1, new ThunderingSparkmage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShepherdOfHeroes());

        castSparkmage(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature with every party type fills only one role")
    void multitypeCreatureFillsOneRole() {
        harness.addToBattlefield(player1, new TajuruParagon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShepherdOfHeroes());

        castSparkmage(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The trigger still deals damage after the Sparkmage leaves")
    void triggerResolvesWithoutSource() {
        harness.addToBattlefield(player1, new ShepherdOfHeroes());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShepherdOfHeroes());
        castSparkmageLeavingTriggerOnStack(target.getId());
        Permanent sparkmage = gd.playerBattlefields.get(player1.getId()).getLast();

        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, sparkmage.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thundering Sparkmage");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Party size decreases when a Cleric leaves before the trigger resolves")
    void usesPartySizeAtResolution() {
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new ShepherdOfHeroes());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShepherdOfHeroes());
        castSparkmageLeavingTriggerOnStack(target.getId());

        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, cleric.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The trigger deals zero damage if its only party member leaves")
    void sourceLeavingCanReducePartyToZero() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShepherdOfHeroes());
        castSparkmageLeavingTriggerOnStack(target.getId());
        Permanent sparkmage = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, sparkmage.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thundering Sparkmage");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void castSparkmage(UUID targetId) {
        castSparkmageLeavingTriggerOnStack(targetId);
        harness.passBothPriorities();
    }

    private void castSparkmageLeavingTriggerOnStack(UUID targetId) {
        harness.setHand(player1, List.of(new ThunderingSparkmage()));
        addSparkmageMana();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
    }

    private void addSparkmageMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
