package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private void castPerilousPredicament() {
        harness.setHand(player1, List.of(new PerilousPredicament()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
