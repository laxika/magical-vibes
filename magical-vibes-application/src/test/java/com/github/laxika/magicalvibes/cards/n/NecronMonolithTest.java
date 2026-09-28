package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NecronMonolith.class, GrizzlyBears.class, Forest.class})
class NecronMonolithTest extends BaseCardTest {

    @Test
    @DisplayName("When it attacks, it mills three and creates a Necron Warrior for each creature milled")
    void attacksMillsAndCreatesOneTokenPerMilledCreature() {
        Card firstCreature = new GrizzlyBears();
        Card nonCreature = new Forest();
        Card secondCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstCreature, nonCreature, secondCreature));
        addReadyMonolithAndCrew();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(firstCreature, nonCreature, secondCreature);
        assertThat(findPermanents(player1, "Necron Warrior")).hasSize(2);
    }

    @Test
    @DisplayName("It creates no tokens when the milled cards are not creatures")
    void doesNotCreateTokensForNoncreatureCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addReadyMonolithAndCrew();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Necron Warrior")).isEmpty();
    }

    private void addReadyMonolithAndCrew() {
        Permanent monolith = harness.addToBattlefieldAndReturn(player1, new NecronMonolith());
        monolith.setSummoningSick(false);
        Permanent firstCrew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        firstCrew.setSummoningSick(false);
        Permanent secondCrew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        secondCrew.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
