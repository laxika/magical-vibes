package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mordenkainen;
import com.github.laxika.magicalvibes.cards.p.PowerWordKill;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuntersMark.class, Cancel.class, CoralMerfolk.class, GrizzlyBears.class, HillGiant.class,
        HillGiantHerdgorger.class, Mordenkainen.class, PowerWordKill.class})
class HuntersMarkTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the source creature before it deals damage equal to its power")
    void boostsSourceBeforeDealingPowerDamage() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new HuntersMark()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), victim.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Costs only {G} when its second target is a blue permanent an opponent controls")
    void reducedCostForBlueOpponentTarget() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new CoralMerfolk());
        harness.setHand(player1, List.of(new HuntersMark()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), victim.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Coral Merfolk");
    }

    @Test
    @DisplayName("Requires the full cost when the second target is not blue")
    void fullCostForNonBlueOpponentTarget() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new HuntersMark()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not reduce the cost for a blue first target you control")
    void blueFirstTargetYouControlDoesNotReduceCost() {
        Permanent source = addCreatureReady(player1, new CoralMerfolk());
        Permanent victim = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new HuntersMark()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature you control as the second target")
    void cannotTargetOwnCreatureAsSecondTarget() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HuntersMark()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature as the first target")
    void cannotTargetOpponentsCreatureAsFirstTarget() {
        Permanent source = addCreatureReady(player2, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new HuntersMark()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        HuntersMark mark = new HuntersMark();
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new CoralMerfolk());
        harness.setHand(player1, List.of(mark));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, mark.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hunter's Mark");
        harness.assertInGraveyard(player2, "Cancel");
        harness.assertInGraveyard(player2, "Coral Merfolk");
    }

    @Test
    void reducedCostAlsoAppliesToBluePlaneswalker() {
        Permanent source = addCreatureReady(player1, new HillGiantHerdgorger());
        Permanent victim = harness.enterBattlefieldAndReturn(player2, new Mordenkainen());
        harness.setHand(player1, List.of(new HuntersMark()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), victim.getId()));

        harness.assertInGraveyard(player2, "Mordenkainen");
        harness.assertOnBattlefield(player1, "Hill Giant Herdgorger");
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(7);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(6);
    }

    @Test
    void removedSourceDoesNotDealDamageUsingLastKnownPower() {
        Permanent source = addCreatureReady(player1, new HillGiantHerdgorger());
        Permanent victim = harness.enterBattlefieldAndReturn(player2, new Mordenkainen());
        harness.setHand(player1, List.of(new HuntersMark()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new PowerWordKill()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant Herdgorger");
        harness.assertOnBattlefield(player2, "Mordenkainen");
        assertThat(victim.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Hunter's Mark");
    }

    @Test
    void removedVictimStillAllowsSourceToBeBoosted() {
        Permanent source = addCreatureReady(player1, new HillGiantHerdgorger());
        Permanent victim = addCreatureReady(player2, new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new HuntersMark()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new PowerWordKill()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, victim.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant Herdgorger");
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(7);
        harness.assertInGraveyard(player1, "Hunter's Mark");
    }

    @Test
    void cannotTargetPlayerAsDamageRecipient() {
        Permanent source = addCreatureReady(player1, new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new HuntersMark()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOwnPlaneswalkerAsDamageRecipient() {
        Permanent source = addCreatureReady(player1, new HillGiantHerdgorger());
        Permanent victim = harness.enterBattlefieldAndReturn(player1, new Mordenkainen());
        harness.setHand(player1, List.of(new HuntersMark()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
