package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AvatarAang;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhirlwindTechnique.class, Forest.class, GrizzlyBears.class, AvatarAang.class})
class WhirlwindTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws two, discards one, and up to two creatures are airbent")
    void drawsDiscardsAndAirbendsTwoCreatures() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WhirlwindTechnique()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId(), firstCreature.getId(), secondCreature.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.findExiledCard(firstCreature.getOriginalCard().getId())).isNull();
        assertThat(gd.findExiledCard(secondCreature.getOriginalCard().getId())).isNull();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.findExiledCard(firstCreature.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(secondCreature.getOriginalCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("Creature targets are optional")
    void canOmitCreatureTargets() {
        harness.setHand(player1, List.of(new WhirlwindTechnique()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot airbend a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new WhirlwindTechnique()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player2.getId(), forest.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Can target yourself and airbend one creature, which its owner can cast for two generic mana")
    void targetsSelfAndOwnerRecastsSingleAirbentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WhirlwindTechnique()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(player1.getId(), creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNull();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castFromExile(player2, creature.getOriginalCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNull();
    }

    @Test
    @DisplayName("A missing creature target does not prevent drawing, discarding, or airbending the other target")
    void resolvesRemainingTargetsWhenOneCreatureLeaves() {
        Permanent missingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent remainingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WhirlwindTechnique()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        addMana();

        harness.castInstant(player1, 0,
                List.of(player2.getId(), missingCreature.getId(), remainingCreature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(missingCreature);
        gd.playerGraveyards.get(player2.getId()).add(missingCreature.getOriginalCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.findExiledCard(missingCreature.getOriginalCard().getId())).isNull();
        assertThat(gd.findExiledCard(remainingCreature.getOriginalCard().getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(missingCreature.getOriginalCard());
    }

    @Test
    @DisplayName("Choosing zero creatures does not trigger Avatar Aang's airbend ability")
    void noCreatureTargetsDoesNotTriggerAirbend() {
        harness.addToBattlefield(player1, new AvatarAang());
        harness.setHand(player1, List.of(new WhirlwindTechnique()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        addMana();

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId()));
        harness.handleCardChosen(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("No airbend ability triggers when the only creature target leaves before resolution")
    void missingOnlyCreatureDoesNotTriggerAirbend() {
        harness.addToBattlefield(player1, new AvatarAang());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WhirlwindTechnique()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        addMana();

        harness.castInstant(player1, 0, List.of(player2.getId(), creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getOriginalCard());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNull();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
