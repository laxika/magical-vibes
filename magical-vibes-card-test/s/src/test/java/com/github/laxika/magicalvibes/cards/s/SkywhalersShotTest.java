package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BuiltToLast;
import com.github.laxika.magicalvibes.cards.e.EagerConstruct;
import com.github.laxika.magicalvibes.cards.l.LongFinnedSkywhale;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkywhalersShot.class, LongFinnedSkywhale.class, EagerConstruct.class,
        BuiltToLast.class, PropheticPrism.class})
class SkywhalersShotTest extends BaseCardTest {

    private void addManaAndCast(UUID targetId) {
        harness.setHand(player1, List.of(new SkywhalersShot()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetId);
    }

    @Test
    @DisplayName("Destroys a creature with power 3 or greater and offers scry 1")
    void destroysHighPowerCreatureAndOffersScry() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new LongFinnedSkywhale());

        addManaAndCast(elemental.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Long-Finned Skywhale");
        harness.assertInGraveyard(player2, "Long-Finned Skywhale");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Completing scry 1 finishes resolving Skywhaler's Shot")
    void completingScryFinishesSpell() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new LongFinnedSkywhale());

        addManaAndCast(elemental.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Skywhaler's Shot");
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 3")
    void cannotTargetLowPowerCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new EagerConstruct());

        assertThatThrownBy(() -> addManaAndCast(creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or greater");
    }

    @Test
    @DisplayName("Can destroy its controller's creature with exactly 3 effective power")
    void destroysOwnCreatureAtPowerThreshold() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EagerConstruct());
        creature.setPowerModifier(1);

        addManaAndCast(creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Eager Construct");
        harness.assertInGraveyard(player1, "Eager Construct");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());

        assertThatThrownBy(() -> addManaAndCast(artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not scry if the target's effective power falls below 3 before resolution")
    void illegalPowerAtResolutionPreventsScry() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LongFinnedSkywhale());
        EagerConstruct top = new EagerConstruct();
        harness.setLibrary(player1, List.of(top));

        addManaAndCast(creature.getId());
        creature.setPowerModifier(-2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Long-Finned Skywhale");
        harness.assertNotInGraveyard(player2, "Long-Finned Skywhale");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Skywhaler's Shot");
    }

    @Test
    @DisplayName("Does not scry if the target leaves the battlefield before resolution")
    void missingTargetPreventsScry() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LongFinnedSkywhale());
        EagerConstruct top = new EagerConstruct();
        harness.setLibrary(player1, List.of(top));

        addManaAndCast(creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.setExile(player2, List.of(creature.getCard()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Skywhaler's Shot");
    }

    @Test
    @DisplayName("Still scries when a legal target is indestructible")
    void scriesEvenWhenDestructionFails() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new EagerConstruct());
        harness.setHand(player2, List.of(new BuiltToLast()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        addManaAndCast(creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Eager Construct");
        harness.assertNotInGraveyard(player2, "Eager Construct");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Scry can keep the top card in place")
    void scryKeepsTopCard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LongFinnedSkywhale());
        EagerConstruct top = new EagerConstruct();
        PropheticPrism bottom = new PropheticPrism();
        harness.setLibrary(player1, List.of(top, bottom));

        addManaAndCast(creature.getId());
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bottom);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scry can put the top card on the bottom")
    void scryMovesTopCardToBottom() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LongFinnedSkywhale());
        EagerConstruct top = new EagerConstruct();
        PropheticPrism bottom = new PropheticPrism();
        harness.setLibrary(player1, List.of(top, bottom));

        addManaAndCast(creature.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, top);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not prevent destruction or completion")
    void resolvesWithEmptyLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LongFinnedSkywhale());
        harness.setLibrary(player1, List.of());

        addManaAndCast(creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Long-Finned Skywhale");
        harness.assertInGraveyard(player1, "Skywhaler's Shot");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
