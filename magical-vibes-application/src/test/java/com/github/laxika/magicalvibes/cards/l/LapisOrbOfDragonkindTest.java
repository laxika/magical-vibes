package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LapisOrbOfDragonkind.class, DragonEgg.class, GrizzlyBears.class})
class LapisOrbOfDragonkindTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for blue mana")
    void tapsForBlueMana() {
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new LapisOrbOfDragonkind());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(orb.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Scries 2 when its mana is spent to cast a Dragon creature")
    void scriesWhenItsManaCastsDragonCreature() {
        harness.addToBattlefield(player1, new LapisOrbOfDragonkind());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new DragonEgg()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Does not trigger for a non-Dragon creature")
    void doesNotTriggerForNonDragonCreature() {
        harness.addToBattlefield(player1, new LapisOrbOfDragonkind());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Does not trigger when a Dragon uses mana from another source")
    void doesNotTriggerForManaFromAnotherSource() {
        harness.addToBattlefield(player1, new LapisOrbOfDragonkind());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new DragonEgg()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }
}
