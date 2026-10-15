package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.k.KothOfTheHammer;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarrageOgre.class, LeoninScimitar.class, LlanowarElves.class, Spellbook.class,
        Memnite.class, KothOfTheHammer.class})
class BarrageOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability with one artifact auto-sacrifices it and puts ability on stack")
    void autoSacrificesOnlyArtifact() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new BarrageOgre());
        harness.addToBattlefield(player1, new Spellbook());

        ogre.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, player2.getId());

        // Auto-sacrificed the only artifact
        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating ability with multiple artifacts asks to choose which to sacrifice")
    void asksForChoiceWithMultipleArtifacts() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new BarrageOgre());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        ogre.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing an artifact to sacrifice puts ability on stack")
    void choosingArtifactPutsAbilityOnStack() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new BarrageOgre());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        ogre.setSummoningSick(false);
        UUID spellbookId = spellbook.getId();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, spellbookId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
        harness.assertNotOnBattlefield(player1, "Spellbook");
        // Leonin Scimitar should still be on battlefield
        harness.assertOnBattlefield(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Ability deals 2 damage to target player on resolution")
    void dealsDamageToPlayer() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new BarrageOgre());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setLife(player2, 20);

        ogre.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Ability deals 2 damage to target creature")
    void dealsDamageToCreature() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new BarrageOgre());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        ogre.setSummoningSick(false);
        UUID elvesId = elves.getId();

        harness.activateAbility(player1, 0, null, elvesId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot activate ability without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new BarrageOgre());

        ogre.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }

    @Test
    @DisplayName("Cannot activate ability when summoning sick (requires tap)")
    void cannotActivateWhenSummoningSick() {
        harness.addToBattlefield(player1, new BarrageOgre());
        harness.addToBattlefield(player1, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap and sacrifice costs are paid before damage resolves, even for a tapped artifact creature")
    void paysCostsBeforeResolution() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new BarrageOgre());
        ogre.setSummoningSick(false);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Memnite());
        artifact.tap();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(ogre.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Memnite");
        harness.assertInGraveyard(player1, "Memnite");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An already tapped Ogre cannot activate or sacrifice an artifact")
    void cannotActivateWhenTapped() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new BarrageOgre());
        ogre.setSummoningSick(false);
        ogre.tap();
        harness.addToBattlefield(player1, new Memnite());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertOnBattlefield(player1, "Memnite");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsArtifact() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new BarrageOgre());
        ogre.setSummoningSick(false);
        harness.addToBattlefield(player2, new Memnite());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");

        harness.assertOnBattlefield(player2, "Memnite");
        assertThat(ogre.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability can target its controller")
    void canDamageController() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new BarrageOgre());
        ogre.setSummoningSick(false);
        harness.addToBattlefield(player1, new Memnite());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Ability deals 2 damage to a planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new BarrageOgre());
        ogre.setSummoningSick(false);
        harness.addToBattlefield(player1, new Memnite());
        Permanent koth = harness.addToBattlefieldAndReturn(player2, new KothOfTheHammer());
        koth.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, null, koth.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Koth of the Hammer");
        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("A targeted artifact creature may be sacrificed as the cost, leaving an illegal target")
    void canSacrificeTargetedArtifactCreature() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new BarrageOgre());
        ogre.setSummoningSick(false);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Memnite());

        harness.activateAbility(player1, 0, null, artifact.getId());

        harness.assertInGraveyard(player1, "Memnite");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Memnite");
        harness.assertOnBattlefield(player1, "Barrage Ogre");
    }

    @Test
    @DisplayName("A noncreature artifact is not a legal damage target")
    void cannotTargetNoncreatureArtifact() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new BarrageOgre());
        ogre.setSummoningSick(false);
        harness.addToBattlefield(player1, new Memnite());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ogre.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Memnite");
        harness.assertOnBattlefield(player2, "Spellbook");
        assertThat(gd.stack).isEmpty();
    }

}
