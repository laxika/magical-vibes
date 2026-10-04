package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FootMystic.class, GrizzlyBears.class, Swamp.class})
class FootMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 black Ninja token after your permanent leaves the battlefield")
    void createsNinjaAfterYourPermanentLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));

        castFootMystic();

        List<Permanent> ninjas = findPermanents(player1, "Ninja");
        assertThat(ninjas).hasSize(1);
        assertThat(ninjas.getFirst().getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(ninjas.getFirst().getCard().getSubtypes()).contains(CardSubtype.NINJA);
        assertThat(gqs.getEffectivePower(gd, ninjas.getFirst())).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ninjas.getFirst())).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a Ninja token when no permanent left the battlefield")
    void doesNotCreateNinjaWithoutPermanentLeaving() {
        castFootMystic();

        assertThat(findPermanents(player1, "Ninja")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's permanent leaving the battlefield does not satisfy Disappear")
    void opponentPermanentLeavingDoesNotCreateNinja() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));

        castFootMystic();

        assertThat(findPermanents(player1, "Ninja")).isEmpty();
    }

    @Test
    @DisplayName("A land leaving satisfies Disappear")
    void landDepartureCreatesNinja() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, land));

        castFootMystic();

        assertThat(findPermanents(player1, "Ninja")).hasSize(1);
    }

    @Test
    @DisplayName("Multiple departures still create only one Ninja per entry")
    void multipleDeparturesCreateOneNinja() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FootMystic());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new FootMystic());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, first);
            harness.getPermanentRemovalService().removePermanentToExile(gd, second);
        });

        castFootMystic();

        assertThat(findPermanents(player1, "Ninja")).hasSize(1);
        assertThat(findPermanents(player2, "Ninja")).isEmpty();
    }

    @Test
    @DisplayName("A departure in an earlier turn does not satisfy Disappear")
    void earlierTurnDepartureDoesNotCreateNinja() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new FootMystic());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        castFootMystic();

        assertThat(findPermanents(player1, "Ninja")).isEmpty();
    }

    @Test
    @DisplayName("A departure after entry cannot retroactively satisfy Disappear")
    void departureAfterEntryDoesNotCreateNinja() {
        harness.castFromHand(player1, new FootMystic(), "{3}{B}");
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        Permanent mystic = findPermanent(player1, "Foot Mystic");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, mystic));

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ninja")).isEmpty();
    }

    @Test
    @DisplayName("The Disappear trigger resolves even after Foot Mystic leaves")
    void triggerSurvivesSourceLeaving() {
        Permanent previous = harness.addToBattlefieldAndReturn(player1, new FootMystic());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, previous));
        harness.castFromHand(player1, new FootMystic(), "{3}{B}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        Permanent mystic = findPermanent(player1, "Foot Mystic");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, mystic));

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ninja")).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Foot Mystic");
    }

    @Test
    @DisplayName("A token leaving also satisfies Disappear")
    void tokenDepartureCreatesNinja() {
        Permanent previous = harness.addToBattlefieldAndReturn(player1, new FootMystic());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, previous));
        castFootMystic();
        Permanent ninja = findPermanent(player1, "Ninja");
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, ninja));

        castFootMystic();

        assertThat(findPermanents(player1, "Ninja")).hasSize(1);
    }

    @Test
    @DisplayName("Foot Mystic gains life from combat damage")
    void lifelinkGainsLife() {
        addCreatureReady(player1, new FootMystic());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    private void castFootMystic() {
        harness.castFromHand(player1, new FootMystic(), "{3}{B}");
        resolveAllTriggers();
    }
}
