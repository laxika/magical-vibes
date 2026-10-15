package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.j.JoustingLance;
import com.github.laxika.magicalvibes.cards.o.OnSerrasWings;
import com.github.laxika.magicalvibes.cards.t.TheAntiquitiesWar;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KembaKhaRegent;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SentinelOfThePearlTrident.class, GrizzlyBears.class, KembaKhaRegent.class,
        Ornithopter.class, JoustingLance.class, OnSerrasWings.class, TheAntiquitiesWar.class})
class SentinelOfThePearlTridentTest extends BaseCardTest {

    @Test
    @CardUsed({SentinelOfThePearlTrident.class, Ornithopter.class})
    @DisplayName("Can exile an artifact you control (historic)")
    void canExileOwnArtifact() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        UUID ornithopterId = harness.getPermanentId(player1, "Ornithopter");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ornithopterId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Ornithopter should be exiled
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ornithopter"));
    }

    @Test
    @CardUsed({SentinelOfThePearlTrident.class, KembaKhaRegent.class})
    @DisplayName("Can exile a legendary creature you control (historic)")
    void canExileLegendaryCreature() {
        harness.addToBattlefield(player1, new KembaKhaRegent());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        UUID kembaId = harness.getPermanentId(player1, "Kemba, Kha Regent");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, kembaId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Kemba, Kha Regent");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Kemba, Kha Regent"));
    }

    @Test
    @CardUsed({SentinelOfThePearlTrident.class, Ornithopter.class})
    @DisplayName("Resolving triggers may ability prompt")
    void resolvingTriggersMayPrompt() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Ornithopter"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @CardUsed({SentinelOfThePearlTrident.class, Ornithopter.class})
    @DisplayName("Declining may ability does not exile anything")
    void decliningMaySkipsExile() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Ornithopter"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sentinel of the Pearl Trident");
        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    @Test
    @CardUsed({SentinelOfThePearlTrident.class, Ornithopter.class})
    @DisplayName("Exiled permanent returns at beginning of next end step")
    void exiledPermanentReturnsAtEndStep() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        UUID ornithopterId = harness.getPermanentId(player1, "Ornithopter");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ornithopterId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Ornithopter is exiled
        harness.assertNotOnBattlefield(player1, "Ornithopter");

        // Advance to end step
        advanceToEndStep();

        // Ornithopter should be back on battlefield
        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Ornithopter"));
    }

    @Test
    @CardUsed({SentinelOfThePearlTrident.class, KembaKhaRegent.class})
    @DisplayName("Returned permanent has summoning sickness")
    void returnedPermanentHasSummoningSickness() {
        harness.addToBattlefield(player1, new KembaKhaRegent());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        UUID kembaId = harness.getPermanentId(player1, "Kemba, Kha Regent");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, kembaId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Kemba, Kha Regent");
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @CardUsed({SentinelOfThePearlTrident.class, GrizzlyBears.class})
    @DisplayName("Non-historic creature you control is not a legal target — no target choice is offered")
    void cannotTargetNonHistoricCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> creature enters; ETB finds no legal target

        // With no legal target, the triggered ability is removed from the stack.
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sentinel of the Pearl Trident");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @CardUsed({SentinelOfThePearlTrident.class, Ornithopter.class})
    @DisplayName("Opponent's historic permanent is not a legal target — no target choice is offered")
    void cannotTargetOpponentHistoricPermanent() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> creature enters; ETB finds no legal target

        // With no legal target, the triggered ability is removed from the stack.
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    @Test
    @CardUsed({SentinelOfThePearlTrident.class, TheAntiquitiesWar.class})
    @DisplayName("A nonlegendary Saga is a legal historic target")
    void canExileSaga() {
        harness.addToBattlefield(player1, new TheAntiquitiesWar());
        castAndExile(harness.getPermanentId(player1, "The Antiquities War"));

        harness.assertNotOnBattlefield(player1, "The Antiquities War");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("The Antiquities War"));
    }

    @Test
    @CardUsed({SentinelOfThePearlTrident.class, JoustingLance.class})
    @DisplayName("An opponent-owned permanent returns to its owner after its controller exiles it")
    void returnsUnderOwnersControl() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, new JoustingLance());
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        castAndExile(stolen.getId());

        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Jousting Lance");
        harness.assertOnBattlefield(player2, "Jousting Lance");
    }

    @Test
    @CardUsed({SentinelOfThePearlTrident.class, JoustingLance.class})
    @DisplayName("The return trigger waits for resolution and survives Sentinel leaving")
    void returnUsesStackAndDoesNotRequireSentinel() {
        harness.addToBattlefield(player1, new JoustingLance());
        castAndExile(harness.getPermanentId(player1, "Jousting Lance"));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player1, "Jousting Lance");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Jousting Lance");
    }

    @Test
    @CardUsed({SentinelOfThePearlTrident.class, JoustingLance.class})
    @DisplayName("Flashing Sentinel in during an end step waits for the following turn's end step")
    void exileDuringEndStepWaitsForFollowingEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new JoustingLance());
        harness.withAutoStop(TurnStep.END_STEP,
                () -> castAndExile(harness.getPermanentId(player1, "Jousting Lance")));

        harness.assertNotOnBattlefield(player1, "Jousting Lance");
        assertThat(gd.stack).isEmpty();
        harness.setLibrary(player1, List.of(new JoustingLance()));
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Jousting Lance");
    }

    @Test
    @CardUsed({SentinelOfThePearlTrident.class, OnSerrasWings.class})
    @DisplayName("A historic Aura remains in exile when there is nothing it can enchant")
    void auraWithoutLegalAttachmentRemainsExiled() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SentinelOfThePearlTrident());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new OnSerrasWings());
        aura.setAttachedTo(creature.getId());
        castAndExile(aura.getId());
        gd.playerBattlefields.get(player1.getId()).clear();

        advanceToEndStep();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("On Serra's Wings"));
        harness.assertNotOnBattlefield(player1, "On Serra's Wings");
        harness.assertNotInGraveyard(player1, "On Serra's Wings");
    }

    @Test
    @CardUsed({SentinelOfThePearlTrident.class, OnSerrasWings.class})
    @DisplayName("The owner chooses a new attachment when a historic Aura returns")
    void auraReturnsAttachedToOwnersChoice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SentinelOfThePearlTrident());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new OnSerrasWings());
        aura.setAttachedTo(creature.getId());
        castAndExile(aura.getId());

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, creature.getId());
        assertThat(findPermanent(player1, "On Serra's Wings").getAttachedTo()).isEqualTo(creature.getId());
        harness.assertNotInGraveyard(player1, "On Serra's Wings");
    }

    private void castAndExile(UUID targetId) {
        harness.setHand(player1, List.of(new SentinelOfThePearlTrident()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
