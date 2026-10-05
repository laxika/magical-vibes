package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CleverConjurer;
import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.y.YoungBlueDragon;
import com.github.laxika.magicalvibes.model.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LapisOrbOfDragonkind.class, DragonEgg.class, GrizzlyBears.class,
        CleverConjurer.class, Naturalize.class, YoungBlueDragon.class})
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

    @Test
    @CardUsed({YoungBlueDragon.class, Naturalize.class})
    @DisplayName("Scries when the produced mana is spent after the Orb is destroyed")
    void scriesAfterOrbLeavesBattlefield() {
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new LapisOrbOfDragonkind());
        harness.setLibrary(player1, List.of(new Naturalize(), new LapisOrbOfDragonkind()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player2, 0, orb.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(orb);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of(new YoungBlueDragon()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).hasSize(2);
    }

    @Test
    @CardUsed({CleverConjurer.class, YoungBlueDragon.class, Naturalize.class})
    @DisplayName("Each mana spent from the same Orb creates a separate scry trigger")
    void triggersForEachManaFromSameOrb() {
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new LapisOrbOfDragonkind());
        addCreatureReady(player1, new CleverConjurer());
        harness.setLibrary(player1, List.of(new Naturalize(), new LapisOrbOfDragonkind()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 0, null, orb.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new YoungBlueDragon()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(3);
    }

    @Test
    @CardUsed({YoungBlueDragon.class, Naturalize.class})
    @DisplayName("Scry looks at two cards and resolves before the Dragon creature spell")
    void resolvesScryTwoBeforeDragon() {
        harness.addToBattlefield(player1, new LapisOrbOfDragonkind());
        Naturalize first = new Naturalize();
        LapisOrbOfDragonkind second = new LapisOrbOfDragonkind();
        YoungBlueDragon third = new YoungBlueDragon();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of(new YoungBlueDragon()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(first, second);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }
}
