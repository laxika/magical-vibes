package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Combust;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoonOfSafety.class, BackupAgent.class, Combust.class, AirElemental.class, GrizzlyBears.class, Murder.class, Shock.class})
class BoonOfSafetyTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a shield counter on the target and scries 1")
    void putsShieldCounterAndScries() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castBoonOfSafety(creature);

        assertThat(creature.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    @DisplayName("A shield counter prevents one damage event")
    void shieldCounterPreventsDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBoonOfSafety(creature);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A shield counter replaces destruction")
    void shieldCounterReplacesDestruction() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBoonOfSafety(creature);

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.getCounterCount(CounterType.SHIELD)).isZero();
    }

    @Test
    @DisplayName("An unpreventable damage event still removes a shield counter")
    void unpreventableDamageRemovesShieldCounter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        castBoonOfSafety(creature);

        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getCounterCount(CounterType.SHIELD)).isZero();
        harness.assertNotOnBattlefield(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Scry can put the top card on the bottom without drawing it")
    void scriesToBottom() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BackupAgent());
        BoonOfSafety top = new BoonOfSafety();
        BackupAgent next = new BackupAgent();
        harness.setLibrary(player1, List.of(top, next));
        harness.setHand(player1, List.of(new BoonOfSafety()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's creature can receive the shield while the caster scries")
    void canTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BackupAgent());
        BoonOfSafety top = new BoonOfSafety();
        harness.setLibrary(player1, List.of(top));
        BackupAgent opponentTop = new BackupAgent();
        harness.setLibrary(player2, List.of(opponentTop));

        castBoonOfSafety(creature);

        assertThat(creature.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An illegal sole target prevents both the shield and the scry")
    void doesNotScryWhenTargetLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BackupAgent());
        BoonOfSafety top = new BoonOfSafety();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new BoonOfSafety(), new Murder()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Backup Agent");
        harness.assertInGraveyard(player1, "Boon of Safety");
        assertThat(creature.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("An empty library does not stop the shield counter being placed")
    void resolvesWithEmptyLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BackupAgent());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new BoonOfSafety()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Boon of Safety");
    }

    @Test
    @DisplayName("Multiple shields lose only one counter per destruction event")
    void multipleShieldsReplaceSeparateDestructionEvents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BackupAgent());
        castBoonOfSafety(creature);
        castBoonOfSafety(creature);
        assertThat(creature.getCounterCount(CounterType.SHIELD)).isEqualTo(2);

        for (int remaining = 1; remaining >= 0; remaining--) {
            harness.setHand(player1, List.of(new Murder()));
            harness.addMana(player1, ManaColor.BLACK, 3);
            harness.castAndResolveInstant(player1, 0, creature.getId());

            assertThat(creature.getCounterCount(CounterType.SHIELD)).isEqualTo(remaining);
            harness.assertOnBattlefield(player1, "Backup Agent");
            assertThat(creature.isTapped()).isFalse();
        }
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.assertNotOnBattlefield(player1, "Backup Agent");
    }

    private void castBoonOfSafety(Permanent target) {
        harness.setHand(player1, List.of(new BoonOfSafety()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }
}
