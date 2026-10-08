package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LavaDart;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TrumpetingHerd;
import com.github.laxika.magicalvibes.cards.v.VolatileClaws;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;




@CardUsed({WrennAndSix.class, Forest.class, Shock.class, SnowCoveredForest.class,
        LavaDart.class, TrumpetingHerd.class, VolatileClaws.class})
class WrennAndSixTest extends BaseCardTest {

    @Test
    @DisplayName("+1 returns a target land card from the graveyard to hand")
    void plusOneReturnsTargetLand() {
        Permanent wrenn = addReadyWrenn(player1, 5);
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        harness.activateAbility(player1, 0, 0, null, forest.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("-1 deals 1 damage to any target")
    void minusOneDealsDamageToAnyTarget() {
        Permanent wrenn = addReadyWrenn(player1, 5);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("-7 grants retrace to instants and sorceries in the graveyard")
    void minusSevenGrantsRetrace() {
        Permanent wrenn = addReadyWrenn(player1, 7);
        Card shock = new Shock();
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(forest));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);

        harness.castRetrace(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void plusOneCanChooseNoTargetWithALandAvailable() {
        Permanent wrenn = addReadyWrenn(player1, 3);
        Card land = new SnowCoveredForest();
        harness.setGraveyard(player1, List.of(land));

        harness.activateAbility(player1, 0, 0, null, null, Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        harness.assertNotInHand(player1, "Snow-Covered Forest");
    }

    @Test
    void plusOneCanBeActivatedWithAnEmptyGraveyard() {
        Permanent wrenn = addReadyWrenn(player1, 3);
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null, Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void plusOneRejectsOpponentsLandAndOwnNonland() {
        addReadyWrenn(player1, 3);
        Card land = new SnowCoveredForest();
        Card spell = new VolatileClaws();
        harness.setGraveyard(player2, List.of(land));
        harness.setGraveyard(player1, List.of(spell));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null,
                land.getId(), Zone.GRAVEYARD)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null,
                spell.getId(), Zone.GRAVEYARD)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusOneCanDamageAPlaneswalker() {
        addReadyWrenn(player1, 3);
        Permanent opposingWrenn = harness.addToBattlefieldAndReturn(player2, new WrennAndSix());
        opposingWrenn.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 1, null, opposingWrenn.getId());
        harness.passBothPriorities();

        assertThat(opposingWrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void emblemAllowsSorceryRetraceRepeatedlyAfterWrennLeaves() {
        addReadyWrenn(player1, 7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Wrenn and Six");

        Card herd = new TrumpetingHerd();
        harness.setGraveyard(player1, List.of(herd));
        harness.setHand(player1, List.of(new SnowCoveredForest(), new SnowCoveredForest()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(herd);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.castRetrace(player1, gd.playerGraveyards.get(player1.getId()).indexOf(herd), 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(herd);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void emblemDoesNotGrantRetraceToOpponentsSpells() {
        addReadyWrenn(player1, 7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setGraveyard(player2, List.of(new VolatileClaws()));
        harness.setHand(player2, List.of(new SnowCoveredForest()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castRetrace(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Snow-Covered Forest");
        harness.assertInGraveyard(player2, "Volatile Claws");
    }

    @Test
    void retraceRequiresDiscardingALand() {
        addReadyWrenn(player1, 7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new VolatileClaws()));
        harness.setHand(player1, List.of(new TrumpetingHerd()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Trumpeting Herd");
        harness.assertInGraveyard(player1, "Volatile Claws");
    }

    @Test
    void emblemAllowsInstantRetraceOnOpponentsTurn() {
        addReadyWrenn(player1, 7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new VolatileClaws()));
        harness.setHand(player1, List.of(new SnowCoveredForest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Volatile Claws");
        harness.assertInGraveyard(player1, "Snow-Covered Forest");
        harness.assertNotInHand(player1, "Snow-Covered Forest");
    }

    @Test
    void emblemDoesNotOverrideSorceryTiming() {
        addReadyWrenn(player1, 7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new TrumpetingHerd()));
        harness.setHand(player1, List.of(new SnowCoveredForest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Trumpeting Herd");
        harness.assertInHand(player1, "Snow-Covered Forest");
    }

    @Test
    void retraceStillRequiresTheSpellsManaCost() {
        addReadyWrenn(player1, 7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new VolatileClaws()));
        harness.setHand(player1, List.of(new SnowCoveredForest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Volatile Claws");
        harness.assertInHand(player1, "Snow-Covered Forest");
    }

    @Test
    void emblemAllowsRetraceInsteadOfExistingFlashback() {
        addReadyWrenn(player1, 7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new LavaDart()));
        harness.setHand(player1, List.of(new SnowCoveredForest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castRetrace(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Lava Dart");
        harness.assertInGraveyard(player1, "Snow-Covered Forest");
        harness.assertNotInHand(player1, "Snow-Covered Forest");
    }

    private Permanent addReadyWrenn(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new WrennAndSix());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}

@CardUsed({WrennAndSix.class, Forest.class, Shock.class, GrizzlyBears.class})
class Mh1WrennAndSixTest extends BaseCardTest {

    @Test
    @DisplayName("+1 returns a target land card from the graveyard to hand")
    void plusOneReturnsLand() {
        Permanent wrenn = addReadyWrenn(player1, 5);
        Card forest = new Forest();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(forest, shock));

        harness.activateAbility(player1, 0, 0, null, forest.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("-1 deals 1 damage to any target")
    void minusOneDealsDamage() {
        Permanent wrenn = addReadyWrenn(player1, 5);
        Permanent shockTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, shockTarget.getId());
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(shockTarget.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("-7 grants retrace to instant and sorcery cards in your graveyard")
    void minusSevenGrantsRetrace() {
        Permanent wrenn = addReadyWrenn(player1, 7);
        Card shock = new Shock();
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(forest));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isZero();

        harness.castRetrace(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Forest");
    }

    private Permanent addReadyWrenn(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new WrennAndSix());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
