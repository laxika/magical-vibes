package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThreeDogGalaxyNewsDJ.class, GrizzlyBears.class, HolyStrength.class, LiquimetalCoating.class})
class ThreeDogGalaxyNewsDJTest extends BaseCardTest {

    @Test
    @DisplayName("Pays and sacrifices an Aura to copy it onto each other attacking creature")
    void copiesSacrificedAuraOntoOtherAttackingCreatures() {
        Permanent dog = addCreatureReady(player1, new ThreeDogGalaxyNewsDJ());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(dog.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, aura.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Holy Strength");
        List<Permanent> copiedAuras = findPermanents(player1, "Holy Strength");
        assertThat(copiedAuras).hasSize(2);
        assertThat(copiedAuras).extracting(Permanent::getAttachedTo)
                .containsExactlyInAnyOrder(firstAttacker.getId(), secondAttacker.getId());
    }

    @Test
    @DisplayName("Declining leaves the Aura and attackers unchanged")
    void decliningDoesNothing() {
        Permanent dog = addCreatureReady(player1, new ThreeDogGalaxyNewsDJ());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(dog.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).doesNotContain(aura.getId());
    }

    @Test
    void copiesAuraWhenThreeDogDoesNotAttack() {
        Permanent dog = addCreatureReady(player1, new ThreeDogGalaxyNewsDJ());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(dog.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(List.of(1));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, aura.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Holy Strength"))
                .singleElement().extracting(Permanent::getAttachedTo).isEqualTo(attacker.getId());
        harness.assertInGraveyard(player1, "Holy Strength");
    }

    @Test
    void attackingWithOnlyThreeDogCanSacrificeAuraWithoutCreatingCopies() {
        Permanent dog = addCreatureReady(player1, new ThreeDogGalaxyNewsDJ());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(dog.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, aura.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Holy Strength");
        harness.assertNotOnBattlefield(player1, "Holy Strength");
    }

    @Test
    void cannotSacrificeAuraAttachedToAnotherCreature() {
        addCreatureReady(player1, new ThreeDogGalaxyNewsDJ());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(attacker.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        harness.assertNotInGraveyard(player1, "Holy Strength");
    }

    @Test
    void cannotSacrificeOpponentsAuraAttachedToThreeDog() {
        Permanent dog = addCreatureReady(player1, new ThreeDogGalaxyNewsDJ());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        aura.setAttachedTo(dog.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(aura);
        harness.assertNotOnBattlefield(player1, "Holy Strength");
    }

    @Test
    void cannotSacrificeWithoutEnoughMana() {
        Permanent dog = addCreatureReady(player1, new ThreeDogGalaxyNewsDJ());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(dog.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(findPermanents(player1, "Holy Strength")).hasSize(1);
        harness.assertNotInGraveyard(player1, "Holy Strength");
    }

    @Test
    void copiesDoNotInheritTemporaryArtifactType() {
        Permanent dog = addCreatureReady(player1, new ThreeDogGalaxyNewsDJ());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(dog.getId());
        harness.addToBattlefield(player1, new LiquimetalCoating());
        harness.activateAbility(player1, 3, null, aura.getId());
        harness.passBothPriorities();
        assertThat(gqs.isArtifact(gd, aura)).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, aura.getId());
        resolveAllTriggers();

        Permanent copy = findPermanents(player1, "Holy Strength").getFirst();
        assertThat(copy.getAttachedTo()).isEqualTo(attacker.getId());
        assertThat(gqs.isArtifact(gd, copy)).isFalse();
    }
}
