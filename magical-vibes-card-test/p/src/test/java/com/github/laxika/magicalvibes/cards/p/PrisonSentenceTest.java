package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.c.CitanulStalwart;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrisonSentence.class, BottleGnomes.class, FountainOfYouth.class, GrizzlyBears.class,
        CitanulStalwart.class})
class PrisonSentenceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Prison Sentence enters a scry 2")
    void resolvingEntersScryTwo() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrisonSentence()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent prisonSentence = new Permanent(new PrisonSentence());
        prisonSentence.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(prisonSentence);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent prisonSentence = new Permanent(new PrisonSentence());
        prisonSentence.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(prisonSentence);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot activate abilities")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent creature = addCreatureReady(player1, new BottleGnomes());

        Permanent prisonSentence = new Permanent(new PrisonSentence());
        prisonSentence.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(prisonSentence);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new PrisonSentence()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void enchantedCreatureCannotActivateManaAbility() {
        Permanent creature = addCreatureReady(player1, new CitanulStalwart());
        Permanent support = addCreatureReady(player1, new CitanulStalwart());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new PrisonSentence());
        aura.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(creature.isTapped()).isFalse();
        assertThat(support.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void abilityCanBeActivatedAfterAuraLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new BottleGnomes());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new PrisonSentence());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(aura);
        gd.playerGraveyards.get(player2.getId()).add(aura.getCard());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.assertLife(player1, 13);
    }

    @Test
    void scryUsesAuraControllersLibraryAndSurvivesAuraLeaving() {
        Permanent creature = addCreatureReady(player2, new CitanulStalwart());
        PrisonSentence first = new PrisonSentence();
        CitanulStalwart second = new CitanulStalwart();
        PrisonSentence third = new PrisonSentence();
        CitanulStalwart opponentsCard = new CitanulStalwart();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setLibrary(player2, List.of(opponentsCard));
        harness.setHand(player1, List.of(new PrisonSentence()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Prison Sentence");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
    }

    @Test
    void illegalTargetOnResolutionDoesNotTriggerScry() {
        Permanent creature = addCreatureReady(player2, new CitanulStalwart());
        PrisonSentence libraryCard = new PrisonSentence();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new PrisonSentence()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Prison Sentence");
        harness.assertInGraveyard(player1, "Prison Sentence");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void scryWithOneCardInLibraryCanPutItOnBottom() {
        Permanent creature = addCreatureReady(player2, new CitanulStalwart());
        PrisonSentence libraryCard = new PrisonSentence();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new PrisonSentence()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(libraryCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }
}
