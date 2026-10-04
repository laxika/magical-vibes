package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AntQueen;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GristTheHungerTide.class, GrizzlyBears.class, AntQueen.class, RestInPeace.class})
class GristTheHungerTideTest extends BaseCardTest {

    @Test
    @DisplayName("Grist is a 1/1 Insect creature outside the battlefield only")
    void becomesCreatureOutsideBattlefield() {
        Card outside = new GristTheHungerTide();
        harness.setHand(player1, List.of(outside));

        assertThat(gqs.cardHasType(outside, CardType.CREATURE, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(outside, CardSubtype.INSECT, gd, player1.getId())).isTrue();

        Permanent onBattlefield = addReadyGrist(player1, 3);
        assertThat(gqs.isCreature(gd, onBattlefield)).isFalse();
        assertThat(gqs.cardHasSubtype(onBattlefield.getCard(), CardSubtype.INSECT, gd, player1.getId()))
                .isFalse();
    }

    @Test
    @DisplayName("+1 creates an Insect, mills, and repeats after milling an Insect")
    void plusOneRepeatsAfterMillingInsect() {
        Permanent grist = addReadyGrist(player1, 3);
        harness.setLibrary(player1, List.of(new AntQueen(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> permanent.getCard().isToken()).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Ant Queen", "Grizzly Bears");
        assertThat(grist.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-2 sacrifices a creature and destroys a target creature")
    void minusTwoSacrificesAndDestroys() {
        Permanent grist = addReadyGrist(player1, 3);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getName())
                .containsExactly("Grist, the Hunger Tide");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(grist.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-5 makes each opponent lose life for creature cards in the controller's graveyard")
    void minusFiveCountsCreatureCardsInControllerGraveyard() {
        Permanent grist = addReadyGrist(player1, 5);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(grist.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("Grist has effective power and toughness of one outside the battlefield")
    void outsideBattlefieldPowerAndToughness() {
        Card grist = new GristTheHungerTide();
        harness.setHand(player1, List.of(grist));
        assertThat(gqs.getEffectiveCardPower(gd, grist)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, grist)).isEqualTo(1);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(grist));
        assertThat(gqs.getEffectiveCardPower(gd, grist)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, grist)).isEqualTo(1);
    }

    @Test
    @DisplayName("+1 creates a token even with an empty library")
    void plusOneWithEmptyLibrary() {
        Permanent grist = addReadyGrist(player1, 3);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, token))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(gqs.cardHasSubtype(token.getCard(), CardSubtype.INSECT, gd, player1.getId())).isTrue();
        assertThat(grist.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("+1 repeats when another Grist is milled as an Insect")
    void plusOneRepeatsForMilledGrist() {
        Permanent grist = addReadyGrist(player1, 3);
        harness.setLibrary(player1, List.of(new GristTheHungerTide()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(2);
        harness.assertInGraveyard(player1, "Grist, the Hunger Tide");
        assertThat(grist.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 repeats for an Insect milled into exile by Rest in Peace")
    void plusOneRepeatsWhenMilledInsectIsExiled() {
        Permanent grist = addReadyGrist(player1, 3);
        harness.addToBattlefield(player2, new RestInPeace());
        harness.setLibrary(player1, List.of(new GristTheHungerTide()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getName)
                .containsExactly("Grist, the Hunger Tide");
        assertThat(grist.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 keeps repeating after Grist leaves the battlefield")
    void plusOneRepeatsWithoutSource() {
        Permanent grist = addReadyGrist(player1, 3);
        harness.setLibrary(player1, List.of(new GristTheHungerTide()));
        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(grist);
        harness.setExile(player1, List.of(grist.getCard()));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(2);
    }

    @Test
    @DisplayName("-2 may be declined without sacrificing or destroying anything")
    void minusTwoMayBeDeclined() {
        Permanent grist = addReadyGrist(player1, 3);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(grist, creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(grist.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("-2 can destroy a planeswalker through a separate reflexive trigger")
    void minusTwoTargetsPlaneswalkerAfterSacrifice() {
        addReadyGrist(player1, 3);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GristTheHungerTide());
        target.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, target.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Grist, the Hunger Tide");
        harness.assertInGraveyard(player2, "Grist, the Hunger Tide");
    }

    @Test
    @DisplayName("-5 counts at resolution, excludes noncreatures and the opponent's graveyard")
    void minusFiveCountsAtResolution() {
        addReadyGrist(player1, 6);
        harness.setGraveyard(player1, List.of(new RestInPeace()));
        harness.setGraveyard(player2, List.of(new GristTheHungerTide()));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.setGraveyard(player1, List.of(new RestInPeace(), new GristTheHungerTide()));

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("-5 causes no life loss when there are no creature cards and Grist survives")
    void minusFiveWithNoCreatureCards() {
        addReadyGrist(player1, 6);
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private Permanent addReadyGrist(Player player, int loyalty) {
        Permanent permanent = addCreatureReady(player, new GristTheHungerTide());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
