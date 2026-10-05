package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.k.KarnScionOfUrza;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrcishVandal.class, Spellbook.class, LeoninScimitar.class, LlanowarElves.class, KarnScionOfUrza.class})
class OrcishVandalTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability with one artifact auto-sacrifices it and puts ability on stack")
    void autoSacrificesOnlyArtifact() {
        addCreatureReady(player1, new OrcishVandal());
        harness.addToBattlefield(player1, new Spellbook());


        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating ability with multiple artifacts asks to choose which to sacrifice")
    void asksForChoiceWithMultipleArtifacts() {
        addCreatureReady(player1, new OrcishVandal());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());


        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing an artifact to sacrifice puts ability on stack")
    void choosingArtifactPutsAbilityOnStack() {
        addCreatureReady(player1, new OrcishVandal());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        UUID spellbookId = findPermanent(player1, "Spellbook").getId();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, spellbookId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertOnBattlefield(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Ability deals 2 damage to target player on resolution")
    void dealsDamageToPlayer() {
        addCreatureReady(player1, new OrcishVandal());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setLife(player2, 20);


        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Ability deals 2 damage to target creature")
    void dealsDamageToCreature() {
        addCreatureReady(player1, new OrcishVandal());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID elvesId = findPermanent(player2, "Llanowar Elves").getId();

        harness.activateAbility(player1, 0, null, elvesId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot activate ability without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        addCreatureReady(player1, new OrcishVandal());


        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }

    @Test
    @DisplayName("Cannot activate ability when summoning sick (requires tap)")
    void cannotActivateWhenSummoningSick() {
        harness.addToBattlefield(player1, new OrcishVandal());
        harness.addToBattlefield(player1, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability deals 2 damage directly to a planeswalker")
    void dealsDamageToPlaneswalker() {
        addCreatureReady(player1, new OrcishVandal());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent karn = harness.addToBattlefieldAndReturn(player2, new KarnScionOfUrza());
        karn.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, null, karn.getId());
        harness.passBothPriorities();

        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Orcish Vandal may target itself")
    void canTargetItself() {
        Permanent vandal = addCreatureReady(player1, new OrcishVandal());
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, vandal.getId());
        assertThat(vandal.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Orcish Vandal");
        harness.assertInGraveyard(player1, "Orcish Vandal");
    }

    @Test
    @DisplayName("Cannot use an opponent's artifact to pay the sacrifice cost")
    void cannotSacrificeOpponentsArtifact() {
        addCreatureReady(player1, new OrcishVandal());
        harness.addToBattlefield(player2, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");

        harness.assertOnBattlefield(player2, "Spellbook");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate a tapped Orcish Vandal")
    void cannotActivateWhenTapped() {
        Permanent vandal = addCreatureReady(player1, new OrcishVandal());
        vandal.setTapped(true);
        harness.addToBattlefield(player1, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Spellbook");
        assertThat(gd.stack).isEmpty();
    }

}
