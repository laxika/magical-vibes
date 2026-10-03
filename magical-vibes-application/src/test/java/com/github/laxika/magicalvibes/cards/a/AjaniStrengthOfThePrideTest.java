package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChandraAcolyteOfFlame;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.ManifoldKey;
import com.github.laxika.magicalvibes.cards.r.Revitalize;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AjaniStrengthOfThePride.class, ChandraAcolyteOfFlame.class, GreenwoodSentinel.class,
        ManifoldKey.class, Revitalize.class})
class AjaniStrengthOfThePrideTest extends BaseCardTest {

    @Test
    @DisplayName("+1 gains life for controlled creatures and planeswalkers")
    void plusOneGainsLifeForCreaturesAndPlaneswalkers() {
        Permanent ajani = addReadyAjani(player1, 4);
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        addReadyChandra(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("-2 creates an Ajani's Pridemate token that grows when its controller gains life")
    void minusTwoCreatesPridemateThatGrowsOnLifeGain() {
        addReadyAjani(player1, 4);
        harness.setLife(player1, 17);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent pridemate = findPermanent(player1, "Ajani's Pridemate");
        assertThat(pridemate.getEffectivePower()).isEqualTo(2);
        assertThat(pridemate.getEffectiveToughness()).isEqualTo(2);

        harness.setLibrary(player1, List.of(new GreenwoodSentinel()));
        harness.setHand(player1, List.of(new Revitalize()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(pridemate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(pridemate.getEffectivePower()).isEqualTo(3);
        assertThat(pridemate.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("0 exiles Ajani and opponents' artifacts and creatures at the life threshold")
    void zeroExilesAjaniAndOpponentsArtifactsAndCreatures() {
        Permanent ajani = addReadyAjani(player1, 4);
        harness.setLife(player1, 35);
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player1, new ManifoldKey());
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        harness.addToBattlefield(player2, new ManifoldKey());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactly("Greenwood Sentinel", "Manifold Key");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Ajani, Strength of the Pride");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Greenwood Sentinel", "Manifold Key");
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("0 does nothing below 15 life above starting life")
    void zeroDoesNothingBelowLifeThreshold() {
        Permanent ajani = addReadyAjani(player1, 4);
        harness.setLife(player1, 34);
        harness.addToBattlefield(player2, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ajani);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactly("Greenwood Sentinel");
    }

    @Test
    void minusTwoStillCreatesTokenWhenPayingCostRemovesAjani() {
        addReadyAjani(player1, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getName())
                .containsExactly("Ajani's Pridemate");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Ajani, Strength of the Pride");
    }

    @Test
    void plusOneCountsOnlyControlledPermanentsAtResolution() {
        addReadyAjani(player1, 4);
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        addReadyChandra(player2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    void zeroUsesCommanderStartingLifeTotal() {
        gd.format = DeckFormat.COMMANDER;
        Permanent ajani = addReadyAjani(player1, 5);
        harness.setLife(player1, 54);
        harness.setLife(player2, 40);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ajani);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void zeroExilesAtCommanderLifeThreshold() {
        gd.format = DeckFormat.COMMANDER;
        addReadyAjani(player1, 5);
        harness.setLife(player1, 55);
        harness.setLife(player2, 40);
        harness.addToBattlefield(player2, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getName)
                .containsExactly("Ajani, Strength of the Pride");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName)
                .containsExactly("Greenwood Sentinel");
    }

    @Test
    void zeroChecksLifeThresholdAtResolutionRatherThanActivation() {
        Permanent ajani = addReadyAjani(player1, 5);
        harness.setLife(player1, 35);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.setLife(player1, 34);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ajani);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void zeroCanBeActivatedBelowThresholdAndExileAfterLifeIncreases() {
        addReadyAjani(player1, 5);
        harness.setLife(player1, 34);
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        Permanent opposingPlaneswalker = addReadyChandra(player2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.setLife(player1, 35);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opposingPlaneswalker);
    }

    @Test
    void pridemateDoesNotTriggerForOpponentsLifeGain() {
        addReadyAjani(player1, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        Permanent pridemate = findPermanent(player1, "Ajani's Pridemate");

        harness.setLibrary(player2, List.of(new GreenwoodSentinel()));
        harness.setHand(player2, List.of(new Revitalize()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(23);
        assertThat(pridemate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReadyAjani(Player player, int loyalty) {
        Permanent perm = addCreatureReady(player, new AjaniStrengthOfThePride());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addReadyChandra(Player player) {
        Permanent perm = addCreatureReady(player, new ChandraAcolyteOfFlame());
        perm.setCounterCount(CounterType.LOYALTY, 6);
        return perm;
    }

}
