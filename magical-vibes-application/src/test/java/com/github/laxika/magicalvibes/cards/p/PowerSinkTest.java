package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlacialChasm;
import com.github.laxika.magicalvibes.cards.s.SavageSummoning;
import com.github.laxika.magicalvibes.cards.s.Scragnoth;
import com.github.laxika.magicalvibes.cards.s.SkyshroudElf;
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

@CardUsed({CanopySpider.class, Forest.class, GlacialChasm.class, PowerSink.class, SavageSummoning.class, Scragnoth.class, SkyshroudElf.class})
class PowerSinkTest extends BaseCardTest {

    private CanopySpider prepareCounterTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        CanopySpider spider = new CanopySpider();
        harness.setHand(player1, List.of(spider));
        return spider;
    }

    @Test
    @DisplayName("Counters and taps lands + empties mana when the controller cannot pay X")
    void countersAndPunishesWhenCannotPay() {
        CanopySpider spider = prepareCounterTarget();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 3); // 2 to cast Spider, 1 left over (< X=3)

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 4); // {U} + X=3

        List<Permanent> p1Battlefield = gd.playerBattlefields.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 3, spider.getId()); // X = 3
        harness.passBothPriorities();

        // Spell countered (player1 could not pay {3}).
        harness.assertInGraveyard(player1, "Canopy Spider");
        harness.assertNotOnBattlefield(player1, "Canopy Spider");
        // Rider: all of player1's lands are tapped and their mana pool is emptied.
        assertThat(p1Battlefield).allMatch(Permanent::isTapped);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @CardUsed(GlacialChasm.class)
    @DisplayName("Does not tap lands without mana abilities")
    void doesNotTapLandsWithoutManaAbilities() {
        CanopySpider spider = prepareCounterTarget();
        Permanent nonManaLand = harness.addToBattlefieldAndReturn(player1, new GlacialChasm());
        Permanent manaLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, spider.getId());
        harness.passBothPriorities();

        assertThat(nonManaLand.isTapped()).isFalse();
        assertThat(manaLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Counters and taps lands + empties mana when the controller declines to pay X")
    void countersAndPunishesWhenDeclines() {
        CanopySpider spider = prepareCounterTarget();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 3); // 2 to cast Spider, 1 left over (can pay X=1)

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 2); // {U} + X=1

        List<Permanent> p1Battlefield = gd.playerBattlefields.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, spider.getId()); // X = 1
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false); // decline to pay

        harness.assertInGraveyard(player1, "Canopy Spider");
        assertThat(p1Battlefield).allMatch(Permanent::isTapped);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Paying X keeps the spell and leaves lands untapped")
    void payingKeepsSpellAndSparesLands() {
        CanopySpider spider = prepareCounterTarget();
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 3); // 2 to cast Spider, 1 to pay X=1

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 2); // {U} + X=1

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, spider.getId()); // X = 1
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true); // pay {1}

        // Not countered and the rider did not fire.
        harness.assertNotInGraveyard(player1, "Canopy Spider");
        assertThat(land.isTapped()).isFalse();

        harness.passBothPriorities(); // resolve Canopy Spider
        harness.assertOnBattlefield(player1, "Canopy Spider");
    }

    @Test
    @DisplayName("Can decline to pay zero and still receive the not-paid rider")
    void canDeclineToPayZero() {
        CanopySpider spider = prepareCounterTarget();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, spider.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Canopy Spider");
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the countered spell's controller loses mana and has lands tapped")
    void riderOnlyAffectsCounteredSpellController() {
        CanopySpider spider = prepareCounterTarget();
        Permanent p1Land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent p2Land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, spider.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Canopy Spider");
        assertThat(p1Land.isTapped()).isTrue();
        assertThat(p2Land.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    @CardUsed(Scragnoth.class)
    @DisplayName("Applies the not-paid rider even when the target spell cannot be countered")
    void appliesNotPaidRiderToUncounterableSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Scragnoth scragnoth = new Scragnoth();
        harness.setHand(player1, List.of(scragnoth));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, scragnoth.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @CardUsed(SavageSummoning.class)
    void appliesNotPaidRiderWhenUncounterableSpellControllerDeclines() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        SavageSummoning summoning = new SavageSummoning();
        harness.setHand(player1, List.of(summoning));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2); // {G} to cast Savage Summoning, {1} remains for X

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 2); // {U} + X=1

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, summoning.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Does not tap non-land permanents with mana abilities")
    void doesNotTapNonLandManaSources() {
        CanopySpider spider = prepareCounterTarget();
        Permanent manaCreature = harness.addToBattlefieldAndReturn(player1, new SkyshroudElf());
        Permanent manaLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, spider.getId());
        harness.passBothPriorities();

        assertThat(manaCreature.isTapped()).isFalse();
        assertThat(manaLand.isTapped()).isTrue();
    }

    @Test
    @CardUsed(Scragnoth.class)
    @DisplayName("Offers payment for an uncounterable spell and leaves it on the stack when paid")
    void offersPaymentForUncounterableSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Scragnoth scragnoth = new Scragnoth();
        harness.setHand(player1, List.of(scragnoth));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 6); // 5 to cast Scragnoth, 1 to pay X

        harness.setHand(player2, List.of(new PowerSink()));
        harness.addMana(player2, ManaColor.BLUE, 2); // {U} + X=1

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, scragnoth.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(land.isTapped()).isFalse();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Scragnoth");
    }
}
