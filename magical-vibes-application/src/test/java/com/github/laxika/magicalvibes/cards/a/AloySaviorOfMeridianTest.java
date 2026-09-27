package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LodestoneGolem;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AloySaviorOfMeridian.class, GrizzlyBears.class, HillGiant.class, LodestoneGolem.class,
        Memnite.class, Plains.class})
class AloySaviorOfMeridianTest extends BaseCardTest {

    @Test
    @DisplayName("Discovers using the greatest power among attacking artifact creatures")
    void discoversUsingGreatestAttackingArtifactCreaturePower() {
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Plains(), discovered));
        harness.addToBattlefield(player1, new AloySaviorOfMeridian());
        addCreatureReady(player1, new LodestoneGolem());
        addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
    }

    @Test
    @DisplayName("Ignores non-artifact attackers when determining discover X")
    void ignoresNonArtifactAttackers() {
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Plains(), discovered));
        harness.addToBattlefield(player1, new AloySaviorOfMeridian());
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player1, new Memnite());

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discovered);
    }

    @Test
    @DisplayName("Does not trigger when no artifact creature attacks")
    void doesNotTriggerWithoutArtifactCreatureAttacker() {
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new AloySaviorOfMeridian());
        addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
