package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TibaltTheFiendBlooded;
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

@CardUsed({BonfireOfTheDamned.class, GrizzlyBears.class, GiantSpider.class, TibaltTheFiendBlooded.class})
class BonfireOfTheDamnedTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to the target player and each creature that player controls")
    void dealsXDamageToPlayerAndTheirCreatures() {
        harness.setHand(player1, List.of(new BonfireOfTheDamned()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2, dies to 3
        harness.addToBattlefield(player2, new GiantSpider());  // 2/4, survives 3
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
        // X is paid twice: 2*3 generic + {R}.
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not damage the caster's own creatures")
    void doesNotDamageCastersCreatures() {
        harness.setHand(player1, List.of(new BonfireOfTheDamned()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cast with X=0 deals no damage")
    void castWithZeroDealsNoDamage() {
        harness.setHand(player1, List.of(new BonfireOfTheDamned()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Miracle cast for {X}{R} only pays X once")
    void miracleCastPaysXOnce() {
        harness.setLibrary(player1, List.of(new BonfireOfTheDamned()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true); // reveal
        harness.passBothPriorities(); // resolve miracle trigger → cast prompt
        harness.handleMayAbilityChosen(player1, true); // cast for miracle cost
        harness.handleXValueChosen(player1, 3);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void damagesPlaneswalkerAndItsControllersCreaturesWithoutDamagingController() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent tibalt = harness.addToBattlefieldAndReturn(player2, new TibaltTheFiendBlooded());
        tibalt.setCounterCount(CounterType.LOYALTY, 2);
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BonfireOfTheDamned()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 2, tibalt.getId());

        harness.assertInGraveyard(player2, "Tibalt, the Fiend-Blooded");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
        assertThat(spider.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    void playerTargetDoesNotDamageTheirPlaneswalkers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent tibalt = harness.addToBattlefieldAndReturn(player2, new TibaltTheFiendBlooded());
        tibalt.setCounterCount(CounterType.LOYALTY, 2);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BonfireOfTheDamned()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player2, "Tibalt, the Fiend-Blooded");
        assertThat(tibalt.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void canTargetCasterAndDamageTheirCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BonfireOfTheDamned()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 2, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BonfireOfTheDamned()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void decliningMiracleRevealLeavesCardInHand() {
        harness.setLibrary(player1, List.of(new BonfireOfTheDamned()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Bonfire of the Damned");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void decliningMiracleCastLeavesRevealedCardInHand() {
        harness.setLibrary(player1, List.of(new BonfireOfTheDamned()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Bonfire of the Damned");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void secondCardDrawnDoesNotOfferMiracle() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new BonfireOfTheDamned()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));

        harness.assertInHand(player1, "Bonfire of the Damned");
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void miracleCanCastSorceryOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new BonfireOfTheDamned()));
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 2);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Bonfire of the Damned");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
