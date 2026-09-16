package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.a.AvenArcher;
import com.github.laxika.magicalvibes.cards.c.CabalShrine;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.e.Execute;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RepentantVampire.class, AvenArcher.class, DuskImp.class, Firebolt.class,
        Execute.class, AngelicWall.class, CabalShrine.class})
class RepentantVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when a creature it damaged in combat dies")
    void getsCounterWhenDamagedCreatureDies() {
        Permanent vampire = addCreatureReady(player1, new RepentantVampire());
        Permanent blocker = addCreatureReady(player2, new AvenArcher());
        vampire.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Aven Archer");
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Threshold makes it white and grants the black-creature destroy ability")
    void thresholdGrantsWhiteColorAndDestroyAbility() {
        fillGraveyard(player1, 7);
        Permanent vampire = addCreatureReady(player1, new RepentantVampire());
        Permanent target = addCreatureReady(player2, new DuskImp());

        assertThat(gqs.getEffectiveColors(gd, vampire)).containsExactly(CardColor.WHITE);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(vampire.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Dusk Imp");
    }

    @Test
    @DisplayName("Threshold abilities are absent below seven cards in the controller's graveyard")
    void thresholdAbilitiesAbsentBelowSevenCards() {
        fillGraveyard(player1, 6);
        Permanent vampire = addCreatureReady(player1, new RepentantVampire());

        assertThat(gqs.hasColor(gd, vampire, CardColor.WHITE)).isFalse();
        assertThat(gs.getEffectiveActivatedAbilities(gd, vampire)).isEmpty();
    }

    @Test
    @DisplayName("Threshold counts cards in this creature's controller's graveyard")
    void thresholdUsesTheControllersGraveyard() {
        fillGraveyard(player2, 7);
        Permanent vampire = addCreatureReady(player1, new RepentantVampire());

        assertThat(gqs.hasColor(gd, vampire, CardColor.WHITE)).isFalse();
        assertThat(gs.getEffectiveActivatedAbilities(gd, vampire)).isEmpty();

        fillGraveyard(player1, 7);

        assertThat(gqs.getEffectiveColors(gd, vampire)).containsExactly(CardColor.WHITE);
        assertThat(gs.getEffectiveActivatedAbilities(gd, vampire)).hasSize(1);
    }

    @Test
    @DisplayName("The threshold ability cannot target a nonblack creature")
    void cannotTargetNonblackCreature() {
        fillGraveyard(player1, 7);
        Permanent vampire = addCreatureReady(player1, new RepentantVampire());
        Permanent target = addCreatureReady(player2, new AvenArcher());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creature");
        assertThat(vampire.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The threshold ability cannot target a black noncreature permanent")
    void cannotTargetBlackNoncreaturePermanent() {
        fillGraveyard(player1, 7);
        Permanent vampire = addCreatureReady(player1, new RepentantVampire());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CabalShrine());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creature");
        assertThat(vampire.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Triggers when a creature damaged by this creature dies later in the same turn")
    void triggersWhenDamagedCreatureDiesLaterThisTurn() {
        Permanent vampire = addCreatureReady(player1, new RepentantVampire());
        Permanent target = addCreatureReady(player2, new AngelicWall());
        vampire.setAttacking(true);
        target.setBlocking(true);
        target.addBlockingTarget(0);

        resolveCombat();

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Angelic Wall");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Angelic Wall");
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an undamaged creature dies")
    void doesNotTriggerWhenUndamagedCreatureDies() {
        Permanent vampire = addCreatureReady(player1, new RepentantVampire());
        Permanent target = addCreatureReady(player2, new AvenArcher());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Aven Archer");
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when the damaged creature dies on a later turn")
    void doesNotTriggerWhenDamagedCreatureDiesOnLaterTurn() {
        Permanent vampire = addCreatureReady(player1, new RepentantVampire());
        Permanent target = addCreatureReady(player2, new AngelicWall());
        vampire.setAttacking(true);
        target.setBlocking(true);
        target.addBlockingTarget(0);

        resolveCombat();

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new Execute()));
        harness.setLibrary(player1, List.of(new AvenArcher()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Angelic Wall");
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new AvenArcher());
        }
        harness.setGraveyard(player, cards);
    }
}
