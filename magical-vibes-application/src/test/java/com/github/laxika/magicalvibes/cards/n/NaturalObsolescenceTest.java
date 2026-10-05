package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NaturalObsolescence.class, FountainOfYouth.class, GrizzlyBears.class, Ornithopter.class})
class NaturalObsolescenceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target artifact on the bottom of its owner's library")
    void putsArtifactOnBottomOfOwnersLibrary() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new NaturalObsolescence()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        int deckSizeBefore = harness.getGameData().playerDecks.get(player2.getId()).size();
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertNotInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(deckSizeBefore + 1)
                .last()
                .extracting(card -> card.getName())
                .isEqualTo("Fountain of Youth");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NaturalObsolescence()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    @DisplayName("Can tuck its controller's artifact creature into an empty library")
    void putsOwnArtifactCreatureIntoEmptyLibrary() {
        Ornithopter artifact = new Ornithopter();
        var target = harness.addToBattlefieldAndReturn(player1, artifact);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new NaturalObsolescence()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
    }

    @Test
    @DisplayName("Uses the artifact's owner rather than its current controller")
    void putsControlledArtifactIntoOwnersLibrary() {
        Ornithopter artifact = new Ornithopter();
        artifact.setOwnerId(player1.getId());
        var target = harness.addToBattlefieldAndReturn(player2, artifact);
        Ornithopter topCard = new Ornithopter();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new NaturalObsolescence()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, artifact);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Ornithopter");
        harness.assertNotInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Does not move a target again after it has left the battlefield")
    void doesNotMoveTargetThatLeftBeforeResolution() {
        Ornithopter artifact = new Ornithopter();
        var target = harness.addToBattlefieldAndReturn(player2, artifact);
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new NaturalObsolescence(), new NaturalObsolescence()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(2)
                .allSatisfy(card -> assertThat(card).isInstanceOf(NaturalObsolescence.class));
        assertThat(gd.stack).isEmpty();
    }
}
