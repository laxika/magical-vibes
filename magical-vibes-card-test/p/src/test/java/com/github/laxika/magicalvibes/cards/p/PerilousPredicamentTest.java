package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImplementOfImprovement;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PerilousPredicament.class, Ornithopter.class, GrizzlyBears.class, ImplementOfImprovement.class})
class PerilousPredicamentTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent sacrifices an artifact creature and a nonartifact creature")
    void sacrificesBothCreatureCategories() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castPerilousPredicament();

        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("If an opponent controls only artifact creatures, only one is sacrificed")
    void onlyArtifactCreaturesRequireOneSacrifice() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castPerilousPredicament();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second).doesNotContain(first);
    }

    @Test
    @DisplayName("When both categories have choices, the selection must include one of each")
    void choiceMustIncludeBothCategories() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent secondArtifactCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent nonartifactCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondNonartifactCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castPerilousPredicament();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player2, List.of(artifactCreature.getId(), secondArtifactCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact creature and a nonartifact creature");

        harness.handleMultiplePermanentsChosen(player2,
                List.of(artifactCreature.getId(), nonartifactCreature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(secondArtifactCreature, secondNonartifactCreature);
    }

    @Test
    @DisplayName("An opponent with only nonartifact creatures chooses exactly one to sacrifice")
    void onlyNonartifactCreaturesRequireOneSacrifice() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castPerilousPredicament();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(first);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Noncreature artifacts are not sacrificed and cannot replace an artifact creature")
    void leavesNoncreatureArtifactsAlone() {
        harness.addToBattlefield(player2, new ImplementOfImprovement());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castPerilousPredicament();

        harness.assertOnBattlefield(player2, "Implement of Improvement");
        harness.assertNotInGraveyard(player2, "Implement of Improvement");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The spell resolves when the opponent controls no creatures")
    void resolvesWithoutOpponentCreatures() {
        castPerilousPredicament();

        harness.assertInGraveyard(player1, "Perilous Predicament");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The spell controller keeps both categories of creatures")
    void controllerDoesNotSacrifice() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castPerilousPredicament();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castPerilousPredicament() {
        harness.castFromHand(player1, new PerilousPredicament(), "{4}{B}");
        harness.passBothPriorities();
    }
}
