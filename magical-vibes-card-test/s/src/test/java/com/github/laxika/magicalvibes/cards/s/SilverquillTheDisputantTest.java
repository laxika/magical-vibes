package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BogwaterLumaret;
import com.github.laxika.magicalvibes.cards.d.DuelTactics;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverquillTheDisputant.class, GrizzlyBears.class, LightningBolt.class,
        Ornithopter.class, BogwaterLumaret.class, DuelTactics.class})
class SilverquillTheDisputantTest extends BaseCardTest {

    @Test
    @DisplayName("Casualty 1 sacrifices a creature and queues a copy")
    void casualtySacrificesCreatureAndQueuesCopy() {
        harness.addToBattlefield(player1, new SilverquillTheDisputant());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithCasualty(player1, 0, player2.getId(), List.of(fodder.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(fodder.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getEffectsToResolve().stream().anyMatch(CopyControllerCastSpellEffect.class::isInstance));
    }

    @Test
    @DisplayName("Casualty can be declined")
    void casualtyCanBeDeclined() {
        harness.addToBattlefield(player1, new SilverquillTheDisputant());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithCasualty(player1, 0, player2.getId(), List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(fodder.getId()));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Casualty 1 requires a creature with power at least one")
    void casualtyRequiresPowerAtLeastOne() {
        harness.addToBattlefield(player1, new SilverquillTheDisputant());
        Permanent fodder = addCreatureReady(player1, new Ornithopter());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithCasualty(player1, 0, player2.getId(), List.of(fodder.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 1");
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(fodder.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sorceryCopyResolvesBeforeOriginalAndKeepsItsTarget() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new SilverquillTheDisputant());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new BogwaterLumaret());
        harness.setHand(player1, List.of(new DuelTactics()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithCasualty(player1, 0, dragon.getId(), List.of(fodder.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().isCopy()).isTrue();
        harness.passBothPriorities();
        assertThat(dragon.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(dragon.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof DuelTactics).hasSize(1);
    }

    @Test
    void sorceryCopyCanChooseANewTarget() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new SilverquillTheDisputant());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new BogwaterLumaret());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player2, new BogwaterLumaret());
        harness.setHand(player1, List.of(new DuelTactics()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithCasualty(player1, 0, dragon.getId(), List.of(fodder.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, newTarget.getId());
        harness.passBothPriorities();

        assertThat(newTarget.getMarkedDamage()).isEqualTo(1);
        assertThat(dragon.getMarkedDamage()).isZero();
        harness.passBothPriorities();
        assertThat(dragon.getMarkedDamage()).isEqualTo(1);
        assertThat(newTarget.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void sacrificingSilverquillRemovesCasualtyBeforeTheSpellIsCast() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new SilverquillTheDisputant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BogwaterLumaret());
        harness.setHand(player1, List.of(new DuelTactics()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithCasualty(player1, 0, target.getId(), List.of(dragon.getId()));

        harness.assertNotOnBattlefield(player1, "Silverquill, the Disputant");
        harness.assertInGraveyard(player1, "Silverquill, the Disputant");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsSpellsDoNotGainCasualty() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new SilverquillTheDisputant());
        Permanent fodder = harness.addToBattlefieldAndReturn(player2, new BogwaterLumaret());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DuelTactics()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithCasualty(player2, 0, dragon.getId(), List.of(fodder.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no matching casualty cost");
        harness.assertOnBattlefield(player2, "Bogwater Lumaret");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureSpellsDoNotGainCasualty() {
        harness.addToBattlefield(player1, new SilverquillTheDisputant());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new BogwaterLumaret());
        harness.setHand(player1, List.of(new BogwaterLumaret()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castWithCasualty(player1, 0, null, List.of(fodder.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no matching casualty cost");
        harness.assertOnBattlefield(player1, "Bogwater Lumaret");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void casualtyIsAvailableForEverySorceryInTheTurn() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new SilverquillTheDisputant());
        Permanent firstFodder = harness.addToBattlefieldAndReturn(player1, new BogwaterLumaret());
        Permanent secondFodder = harness.addToBattlefieldAndReturn(player1, new BogwaterLumaret());
        harness.setHand(player1, List.of(new DuelTactics(), new DuelTactics()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castWithCasualty(player1, 0, dragon.getId(), List.of(firstFodder.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.castWithCasualty(player1, 0, dragon.getId(), List.of(secondFodder.getId()));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstFodder, secondFodder);
    }

    @Test
    void casualtyCannotSacrificeAnOpponentsCreature() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new SilverquillTheDisputant());
        Permanent opponentsCreature = harness.addToBattlefieldAndReturn(player2, new BogwaterLumaret());
        harness.setHand(player1, List.of(new DuelTactics()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithCasualty(player1, 0, dragon.getId(),
                List.of(opponentsCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Bogwater Lumaret");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void aSingleCasualtyAbilityCannotSacrificeTwoCreatures() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new SilverquillTheDisputant());
        Permanent firstFodder = harness.addToBattlefieldAndReturn(player1, new BogwaterLumaret());
        Permanent secondFodder = harness.addToBattlefieldAndReturn(player1, new BogwaterLumaret());
        harness.setHand(player1, List.of(new DuelTactics()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithCasualty(player1, 0, dragon.getId(),
                List.of(firstFodder.getId(), secondFodder.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no matching casualty cost");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstFodder, secondFodder);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
