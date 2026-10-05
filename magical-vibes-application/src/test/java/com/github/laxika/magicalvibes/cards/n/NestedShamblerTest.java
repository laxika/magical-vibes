package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.t.TragicFall;
import com.github.laxika.magicalvibes.cards.v.VerminGorger;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NestedShambler.class, GiantGrowth.class, WrathOfGod.class, TragicFall.class, VerminGorger.class})
class NestedShamblerTest extends BaseCardTest {

    @Test
    @DisplayName("When Nested Shambler dies, it creates tapped green Squirrels equal to its power")
    void deathCreatesTappedSquirrelsEqualToPower() {
        Permanent shambler = harness.addToBattlefieldAndReturn(player1, new NestedShambler());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, shambler.getId());

        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> squirrels = findPermanents(player1, "Squirrel");
        assertThat(squirrels).hasSize(4);
        assertThat(squirrels).allSatisfy(squirrel -> {
            assertThat(squirrel.isTapped()).isTrue();
            assertThat(squirrel.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(squirrel.getCard().getSubtypes()).contains(CardSubtype.SQUIRREL);
            assertThat(squirrel.getEffectivePower()).isEqualTo(1);
            assertThat(squirrel.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Sacrificing an unmodified Nested Shambler creates one tapped Squirrel")
    void sacrificeCreatesOneTappedSquirrel() {
        addCreatureReady(player1, new VerminGorger());
        harness.addToBattlefield(player1, new NestedShambler());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nested Shambler");
        assertThat(findPermanents(player1, "Squirrel")).singleElement()
                .satisfies(squirrel -> assertThat(squirrel.isTapped()).isTrue());
        assertThat(findPermanents(player2, "Squirrel")).isEmpty();
    }

    @Test
    @DisplayName("Nested Shambler dying with negative power creates no Squirrels")
    void negativePowerCreatesNoSquirrels() {
        Permanent shambler = harness.addToBattlefieldAndReturn(player1, new NestedShambler());
        harness.setHand(player1, List.of(new TragicFall(), new NestedShambler()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, shambler.getId());
        harness.assertInGraveyard(player1, "Nested Shambler");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Squirrel")).isEmpty();
        assertThat(findPermanents(player2, "Squirrel")).isEmpty();
    }

    @Test
    @DisplayName("Simultaneously dying Shamblers create Squirrels for their respective controllers")
    void simultaneousDeathsCreateTokensForEachController() {
        harness.addToBattlefield(player1, new NestedShambler());
        harness.addToBattlefield(player2, new NestedShambler());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nested Shambler");
        harness.assertInGraveyard(player2, "Nested Shambler");
        assertThat(findPermanents(player1, "Squirrel")).singleElement()
                .satisfies(squirrel -> assertThat(squirrel.isTapped()).isTrue());
        assertThat(findPermanents(player2, "Squirrel")).singleElement()
                .satisfies(squirrel -> assertThat(squirrel.isTapped()).isTrue());
    }
}
