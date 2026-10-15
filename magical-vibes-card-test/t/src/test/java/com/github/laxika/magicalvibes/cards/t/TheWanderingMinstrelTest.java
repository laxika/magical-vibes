package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SazhsChocobo;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWanderingMinstrel.class, Forest.class, SazhsChocobo.class, TrenoDarkCity.class})
class TheWanderingMinstrelTest extends BaseCardTest {

    @Test
    @DisplayName("Controlled lands enter untapped")
    void controlledLandsEnterUntapped() {
        harness.addToBattlefield(player1, new TheWanderingMinstrel());
        harness.setHand(player1, List.of(new TrenoDarkCity()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, "Untapped");

        assertThat(findPermanent(player1, "Treno, Dark City").isTapped()).isFalse();
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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());
        addTowns(player1, 3);
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(minstrel.getPowerModifier()).isZero();
        assertThat(minstrel.getToughnessModifier()).isZero();
        assertThat(bears.getPowerModifier()).isEqualTo(3);
        assertThat(bears.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    void canChooseForTownToEnterTapped() {
        harness.addToBattlefield(player1, new TheWanderingMinstrel());
        harness.setHand(player1, List.of(new TrenoDarkCity()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, "Tapped");

        assertThat(findPermanent(player1, "Treno, Dark City").isTapped()).isTrue();
    }

    @Test
    void doesNotUntapOpponentsLands() {
        harness.addToBattlefield(player1, new TheWanderingMinstrel());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new TrenoDarkCity()));

        harness.playLand(player2, 0);

        assertThat(findPermanent(player2, "Treno, Dark City").isTapped()).isTrue();
    }

    @Test
    void doesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new TheWanderingMinstrel());
        addTowns(player1, 5);

        beginCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void opponentsTownsDoNotMeetThreshold() {
        harness.addToBattlefield(player1, new TheWanderingMinstrel());
        addTowns(player1, 4);
        addTowns(player2, 5);

        beginCombat(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rechecksTownThresholdOnResolution() {
        harness.addToBattlefield(player1, new TheWanderingMinstrel());
        addTowns(player1, 5);
        beginCombat(player1);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void gainingFifthTownAfterBeginningOfCombatDoesNotCreateTrigger() {
        harness.addToBattlefield(player1, new TheWanderingMinstrel());
        addTowns(player1, 4);
        beginCombat(player1);
        assertThat(gd.stack).isEmpty();

        addTowns(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void boostCountsTownsAtResolutionAndRemainsFixedAfterwards() {
        harness.addToBattlefield(player1, new TheWanderingMinstrel());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SazhsChocobo());
        addTowns(player1, 2);
        addTowns(player2, 5);
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        addTowns(player1, 1);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(3);
        assertThat(opponentCreature.getPowerModifier()).isZero();
        addTowns(player1, 1);
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());
        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(newcomer.getPowerModifier()).isZero();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    void zeroTownsGivesNoBoost() {
        harness.addToBattlefield(player1, new TheWanderingMinstrel());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());
        harness.addToBattlefield(player1, new Forest());
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    private void addTowns(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new TrenoDarkCity());
        }
    }

    private void beginCombat(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
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
