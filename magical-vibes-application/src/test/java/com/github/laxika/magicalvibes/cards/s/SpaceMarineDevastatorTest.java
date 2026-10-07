package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.t.TheFleshIsWeak;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpaceMarineDevastator.class, GildedLotus.class, TheFleshIsWeak.class})
class SpaceMarineDevastatorTest extends BaseCardTest {

    @Test
    @DisplayName("Squad creates one token copy for each additional payment")
    void squadCreatesTokenCopies() {
        harness.setHand(player1, List.of(new SpaceMarineDevastator()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{2}", "{2}"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Space Marine Devastator")).hasSize(3);
        assertThat(findPermanents(player1, "Space Marine Devastator"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("Grav-cannon destroys a target artifact when it enters")
    void gravCannonDestroysArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        harness.setHand(player1, List.of(new SpaceMarineDevastator()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, artifact.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Gilded Lotus");
        harness.assertOnBattlefield(player1, "Space Marine Devastator");
    }

    @Test
    @DisplayName("Grav-cannon cannot target a creature")
    void gravCannonRejectsCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SpaceMarineDevastator());
        harness.setHand(player1, List.of(new SpaceMarineDevastator()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    @Test
    @DisplayName("Grav-cannon destroys an enchantment")
    void gravCannonDestroysEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new TheFleshIsWeak());
        harness.setHand(player1, List.of(new SpaceMarineDevastator()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, enchantment.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "The Flesh Is Weak");
        harness.assertOnBattlefield(player1, "Space Marine Devastator");
    }

    @Test
    @DisplayName("No Squad payments creates no token copies")
    void noSquadPaymentsCreatesNoCopies() {
        harness.setHand(player1, List.of(new SpaceMarineDevastator()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Space Marine Devastator")).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Grav-cannon may decline a legal target without losing Squad copies")
    void decliningTargetStillCreatesSquadCopies() {
        harness.addToBattlefield(player2, new GildedLotus());
        harness.setHand(player1, List.of(new SpaceMarineDevastator()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{2}"));
        resolveAllTriggers();
        for (int trigger = 0; trigger < 2 && gd.interaction.isAwaitingInput(); trigger++) {
            harness.handlePermanentChosen(player1, player1.getId());
            resolveAllTriggers();
        }

        harness.assertOnBattlefield(player2, "Gilded Lotus");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Space Marine Devastator")).hasSize(2);
        assertThat(findPermanents(player1, "Space Marine Devastator"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Squad and Grav-cannon put two independent abilities on the stack")
    void squadAndGravCannonTriggerIndependently() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        harness.setHand(player1, List.of(new SpaceMarineDevastator()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{2}"));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("A Squad token's Grav-cannon can destroy another artifact")
    void squadTokenDestroysAnotherArtifact() {
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        harness.setHand(player1, List.of(new SpaceMarineDevastator()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{2}"));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, firstArtifact.getId());
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, secondArtifact.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Gilded Lotus")).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Gilded Lotus")).hasSize(2);
        assertThat(findPermanents(player1, "Space Marine Devastator")).hasSize(2);
    }
}
