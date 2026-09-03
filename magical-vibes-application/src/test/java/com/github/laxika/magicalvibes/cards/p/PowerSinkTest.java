package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlacialChasm;
import com.github.laxika.magicalvibes.cards.s.SavageSummoning;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PowerSink.class, BalduvianBears.class, Forest.class, GlacialChasm.class, BirdsOfParadise.class, EvolvingWilds.class})
class PowerSinkTest extends BaseCardTest {

    private BalduvianBears prepareCounterTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        BalduvianBears bears = new BalduvianBears();
        harness.setHand(player1, List.of(bears));
        return bears;
    }

    // ===== Can't pay X → countered, plus the not-paid rider fires =====

    @Test
    @DisplayName("Counters and taps lands + empties mana when the controller cannot pay X")
    void countersAndPunishesWhenCannotPay() {
        BalduvianBears bears = prepareCounterTarget();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 3); // 2 to cast Bears, 1 left over (< X)

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 4); // {U} + X=3

        List<Permanent> p1Battlefield = gd.playerBattlefields.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 3, bears.getId()); // X = 3
        harness.passBothPriorities();

        // Spell countered (player1 could not pay {3}).
        harness.assertInGraveyard(player1, "Balduvian Bears");
        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        // Rider: all of player1's lands are tapped and their mana pool is emptied.
        assertThat(p1Battlefield).allMatch(Permanent::isTapped);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Does not tap lands without mana abilities")
    void doesNotTapLandsWithoutManaAbilities() {
        BalduvianBears bears = prepareCounterTarget();
        Permanent nonManaLand = harness.addToBattlefieldAndReturn(player1, new GlacialChasm());
        Permanent manaLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, bears.getId());
        harness.passBothPriorities();

        assertThat(nonManaLand.isTapped()).isFalse();
        assertThat(manaLand.isTapped()).isTrue();
    }

    // ===== Declines to pay X → countered, plus the not-paid rider fires =====

    @Test
    @DisplayName("Counters and taps lands + empties mana when the controller declines to pay X")
    void countersAndPunishesWhenDeclines() {
        BalduvianBears bears = prepareCounterTarget();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 3); // 2 to cast Bears, 1 left over (can pay X=1)

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 2); // {U} + X=1

        List<Permanent> p1Battlefield = gd.playerBattlefields.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, bears.getId()); // X = 1
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false); // decline to pay

        harness.assertInGraveyard(player1, "Balduvian Bears");
        assertThat(p1Battlefield).allMatch(Permanent::isTapped);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    // ===== Pays X → not countered, rider does not fire =====

    @Test
    @DisplayName("Paying X keeps the spell and leaves lands untapped")
    void payingKeepsSpellAndSparesLands() {
        BalduvianBears bears = prepareCounterTarget();
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 3); // 2 to cast Bears, 1 to pay X=1

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 2); // {U} + X=1

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, bears.getId()); // X = 1
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true); // pay {1}

        // Not countered and the rider did not fire.
        harness.assertNotInGraveyard(player1, "Balduvian Bears");
        assertThat(land.isTapped()).isFalse();

        harness.passBothPriorities(); // resolve Balduvian Bears
        harness.assertOnBattlefield(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Can decline to pay zero and still receive the not-paid rider")
    void canDeclineToPayZero() {
        BalduvianBears bears = prepareCounterTarget();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Balduvian Bears");
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the countered spell's controller loses mana and has lands tapped")
    void riderOnlyAffectsCounteredSpellController() {
        BalduvianBears bears = prepareCounterTarget();
        Permanent p1Land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent p2Land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Balduvian Bears");
        assertThat(p1Land.isTapped()).isTrue();
        assertThat(p2Land.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    @CardUsed(SavageSummoning.class)
    @DisplayName("Applies the not-paid rider even when the target spell cannot be countered")
    void appliesNotPaidRiderToUncounterableSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        SavageSummoning summoning = new SavageSummoning();
        harness.setHand(player1, List.of(summoning));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, summoning.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not tap non-land permanents with mana abilities")
    void doesNotTapNonLandManaSources() {
        BalduvianBears bears = prepareCounterTarget();
        Permanent manaCreature = harness.addToBattlefieldAndReturn(player1, new BirdsOfParadise());
        Permanent manaLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, bears.getId());
        harness.passBothPriorities();

        assertThat(manaCreature.isTapped()).isFalse();
        assertThat(manaLand.isTapped()).isTrue();
    }
}
