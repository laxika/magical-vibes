package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuidingBolt.class, AirElemental.class, GrizzlyBears.class, Unsummon.class})
class GuidingBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with power 4 or greater")
    void destroysCreatureAtPowerThreshold() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new GuidingBolt()));
        addGuidingBoltMana();
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetLowPowerCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GuidingBolt()));
        addGuidingBoltMana();
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Scries 2 after destroying the target")
    void scriesAfterDestroyingTarget() {
        harness.addToBattlefield(player2, new AirElemental());
        List<Card> library = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new GuidingBolt()));
        addGuidingBoltMana();
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(library.get(0), library.get(1));

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(1), library.get(2), library.get(0));
    }

    @Test
    @DisplayName("Does not scry when the only target leaves the battlefield")
    void doesNotScryWhenTargetLeavesBattlefield() {
        harness.addToBattlefield(player2, new AirElemental());
        List<Card> library = List.of(new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new GuidingBolt()));
        harness.setHand(player2, List.of(new Unsummon()));
        addGuidingBoltMana();
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Guiding Bolt");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
    }

    @Test
    @DisplayName("Can destroy its controller's creature and scry with only one library card")
    void destroysOwnCreatureAndScriesShortLibrary() {
        harness.addToBattlefield(player1, new AirElemental());
        Card libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new GuidingBolt()));
        addGuidingBoltMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Air Elemental"));

        harness.assertInGraveyard(player1, "Air Elemental");
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(libraryCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertInGraveyard(player1, "Guiding Bolt");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Still destroys the target when the controller's library is empty")
    void destroysTargetWithEmptyLibrary() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new GuidingBolt()));
        addGuidingBoltMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Air Elemental"));

        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Guiding Bolt");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void addGuidingBoltMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
