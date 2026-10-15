package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.t.ThrunTheLastTroll;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KadenasSilencer.class, IcyManipulator.class, LightningBolt.class, ThrunTheLastTroll.class})
class KadenasSilencerTest extends BaseCardTest {

    @Test
    void turningFaceUpCountersOpponentActivatedAbilities() {
        Permanent silencer = castFaceDownSilencer();

        harness.addToBattlefield(player2, new IcyManipulator());
        Permanent icyManipulator = findPermanent(player2, "Icy Manipulator");
        icyManipulator.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(icyManipulator), null,
                silencer.getId());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Icy Manipulator"));

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(silencer));
        resolveAllTriggers();

        assertThat(silencer.isTapped()).isFalse();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Icy Manipulator"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    void payingMegamorphCostAddsCounterBeforeTriggerResolves() {
        Permanent silencer = castFaceDownSilencer();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(silencer));

        assertThat(silencer.isFaceDown()).isFalse();
        assertThat(silencer.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(silencer.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    void countersOpponentTriggeredAbility() {
        Permanent opponentSilencer = castFaceDownSilencer(player2);
        Permanent silencer = castFaceDownSilencer();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(opponentSilencer));
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(silencer));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Kadena's Silencer");
    }

    @Test
    void countersMultipleOpponentAbilitiesButPreservesControllersAbility() {
        Permanent silencer = castFaceDownSilencer();
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.addToBattlefield(player1, new IcyManipulator());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, silencer.getId());
        harness.activateAbility(player2, 1, null, silencer.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent opponentIcy = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.activateAbility(player1, 1, null, opponentIcy.getId());
        assertThat(gd.stack).hasSize(3);

        harness.turnFaceUp(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        resolveAllTriggers();
        assertThat(silencer.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Icy Manipulator");
    }

    @Test
    void countersAbilityEvenWhenItsSourceSpellCannotBeCountered() {
        Permanent silencer = castFaceDownSilencer();
        harness.addToBattlefield(player2, new ThrunTheLastTroll());
        Permanent thrun = findPermanent(player2, "Thrun, the Last Troll");
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(silencer));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(thrun.getRegenerationShield()).isZero();
        harness.assertOnBattlefield(player2, "Thrun, the Last Troll");
    }

    private Permanent castFaceDownSilencer() {
        return castFaceDownSilencer(player1);
    }

    private Permanent castFaceDownSilencer(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new KadenasSilencer()));
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player, "Kadena's Silencer");
    }
}
