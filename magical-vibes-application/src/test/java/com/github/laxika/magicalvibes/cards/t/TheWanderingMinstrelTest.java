package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PollutedMire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWanderingMinstrel.class, Forest.class, GrizzlyBears.class, PollutedMire.class})
class TheWanderingMinstrelTest extends BaseCardTest {

    @Test
    @DisplayName("Controlled lands enter untapped")
    void controlledLandsEnterUntapped() {
        harness.addToBattlefield(player1, new TheWanderingMinstrel());
        harness.setHand(player1, List.of(new PollutedMire()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Polluted Mire").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creates an all-color Elemental at beginning of combat with five Towns")
    void createsElementalWithFiveTowns() {
        harness.addToBattlefield(player1, new TheWanderingMinstrel());
        addTowns(player1, 5);

        beginCombat(player1);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Elemental");
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK,
                        CardColor.RED, CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("Does not create the Elemental with fewer than five Towns")
    void doesNotCreateElementalBelowTownThreshold() {
        harness.addToBattlefield(player1, new TheWanderingMinstrel());
        addTowns(player1, 4);

        beginCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Boosts other creatures by the number of Towns")
    void boostsOtherCreaturesByTownCount() {
        Permanent minstrel = harness.addToBattlefieldAndReturn(player1, new TheWanderingMinstrel());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addTowns(player1, 3);
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(minstrel.getPowerModifier()).isZero();
        assertThat(minstrel.getToughnessModifier()).isZero();
        assertThat(bears.getPowerModifier()).isEqualTo(3);
        assertThat(bears.getToughnessModifier()).isEqualTo(3);
    }

    private void addTowns(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            Card town = TestCards.mutableCard(new Permanent(new Forest()));
            town.setSubtypes(List.of(CardSubtype.TOWN));
            harness.addToBattlefield(player, town);
        }
    }

    private void beginCombat(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
