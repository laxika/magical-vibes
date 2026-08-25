package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SubterraneanSchooner.class, GrizzlyBears.class, Forest.class})
class SubterraneanSchoonerTest extends BaseCardTest {

    @Test
    void attackTriggerOnlyTargetsCreatureThatCrewedThisTurn() {
        Permanent schooner = addReadySchooner();
        Permanent crewer = addCreatureReady(player1, new GrizzlyBears());
        Permanent bystander = addCreatureReady(player1, new GrizzlyBears());

        crewSchooner();
        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(crewer.getId());
        assertThat(schooner.isTapped()).isTrue();
        assertThat(bystander.isTapped()).isFalse();
    }

    @Test
    void targetedCreatureExploresWhenAttackTriggerResolves() {
        addReadySchooner();
        Permanent crewer = addCreatureReady(player1, new GrizzlyBears());
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));

        crewSchooner();
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crewer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topLand);
    }

    private Permanent addReadySchooner() {
        Permanent schooner = harness.addToBattlefieldAndReturn(player1, new SubterraneanSchooner());
        schooner.setSummoningSick(false);
        return schooner;
    }

    private void crewSchooner() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
