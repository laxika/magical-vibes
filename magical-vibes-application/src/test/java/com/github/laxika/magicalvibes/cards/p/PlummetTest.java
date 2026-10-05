package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.ArmoredAscension;
import com.github.laxika.magicalvibes.cards.c.CloudElemental;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.GameData;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Plummet.class, CloudElemental.class, RuneclawBear.class, ArmoredAscension.class})
class PlummetTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Plummet targeting a creature with flying puts it on stack")
    void castingPutsOnStack() {
        Permanent cloudElemental = harness.addToBattlefieldAndReturn(player2, new CloudElemental());

        harness.setHand(player1, List.of(new Plummet()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, cloudElemental.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(Plummet.class);
        assertThat(entry.getTargetId()).isEqualTo(cloudElemental.getId());
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        // Add a creature with flying as valid target so spell is playable
        harness.addToBattlefield(player1, new CloudElemental());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new Plummet()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    @DisplayName("Resolving Plummet destroys target creature with flying")
    void resolvingDestroysTargetCreature() {
        Permanent cloudElemental = harness.addToBattlefieldAndReturn(player2, new CloudElemental());

        harness.setHand(player1, List.of(new Plummet()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, cloudElemental.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cloud Elemental");
        harness.assertInGraveyard(player2, "Cloud Elemental");
        harness.assertInGraveyard(player1, "Plummet");
    }

    @Test
    @DisplayName("Plummet fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent cloudElemental = harness.addToBattlefieldAndReturn(player2, new CloudElemental());

        harness.setHand(player1, List.of(new Plummet()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, cloudElemental.getId());
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Plummet");
    }

    @Test
    @DisplayName("Plummet can destroy its controller's flying creature")
    void destroysOwnFlyingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CloudElemental());
        harness.setHand(player1, List.of(new Plummet()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cloud Elemental");
        harness.assertInGraveyard(player1, "Cloud Elemental");
        harness.assertInGraveyard(player1, "Plummet");
    }

    @Test
    @DisplayName("Plummet destroys a creature with flying granted by an Aura")
    void destroysCreatureWithGrantedFlying() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ArmoredAscension());
        aura.setAttachedTo(bear.getId());
        harness.setHand(player1, List.of(new Plummet()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Plummet");
    }

    @Test
    @DisplayName("Plummet does not resolve if its target loses flying")
    void doesNotDestroyTargetThatLosesFlying() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new ArmoredAscension());
        aura.setAttachedTo(bear.getId());
        harness.setHand(player1, List.of(new Plummet()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, bear.getId());
        gd.playerBattlefields.get(player2.getId()).remove(aura);
        gd.playerGraveyards.get(player2.getId()).add(aura.getCard());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        harness.assertNotInGraveyard(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Plummet");
        assertThat(gd.stack).isEmpty();
    }
}
