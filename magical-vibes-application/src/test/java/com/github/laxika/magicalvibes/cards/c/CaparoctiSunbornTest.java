package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IzzetCluestone;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaparoctiSunborn.class, Forest.class, GrizzlyBears.class, IzzetCluestone.class})
class CaparoctiSunbornTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking can tap two artifacts and/or creatures to discover 3")
    void attackingCanTapTwoPermanentsToDiscover() {
        Permanent caparocti = addCreatureReady(player1, new CaparoctiSunborn());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IzzetCluestone());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), discovered));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(artifact.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
        assertThat(caparocti.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the attack trigger does not tap permanents or discover")
    void decliningAttackTriggerDoesNothing() {
        addCreatureReady(player1, new CaparoctiSunborn());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IzzetCluestone());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(artifact.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
