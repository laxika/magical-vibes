package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SplendidReclamation;
import com.github.laxika.magicalvibes.cards.t.TheGitrogMonster;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MythweaverPoq.class, Forest.class, Plains.class, TheGitrogMonster.class, EvolvingWilds.class, SplendidReclamation.class})
class MythweaverPoqTest extends BaseCardTest {

    @Test
    void powerAndToughnessEqualControlledLands() {
        Permanent poq = addCreatureReady(player1, new MythweaverPoq());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, poq)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, poq)).isEqualTo(2);
    }

    @Test
    void landfallConjuresNonTokenDuplicateOfEnteringLand() {
        addCreatureReady(player1, new MythweaverPoq());
        harness.addToBattlefield(player1, new Forest());
        Card enteringLand = new Plains();
        harness.setHand(player1, List.of(enteringLand));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        List<Permanent> lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND))
                .toList();
        assertThat(lands).hasSize(3);
        assertThat(lands).allMatch(permanent -> !permanent.getCard().isToken());
        assertThat(lands).extracting(permanent -> permanent.getCard().getId())
                .contains(enteringLand.getId())
                .doesNotHaveDuplicates();
    }

    @Test
    void landfallTriggersOnlyOnceEachTurn() {
        addCreatureReady(player1, new MythweaverPoq());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new TheGitrogMonster());
        harness.setHand(player1, List.of(new Plains(), new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        long landCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND))
                .count();
        assertThat(landCount).isEqualTo(4);
    }

    @Test
    @CardUsed({MythweaverPoq.class, Forest.class, Plains.class, SplendidReclamation.class})
    void simultaneousLandsEachReceiveADuplicate() {
        harness.addToBattlefield(player1, new Forest());
        addCreatureReady(player1, new MythweaverPoq());
        harness.setGraveyard(player1, List.of(new Forest(), new Plains()));

        harness.castFromHand(player1, new SplendidReclamation(), "{3}{G}");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Forest")).isEqualTo(3);
        assertThat(countPermanents(player1, "Plains")).isEqualTo(2);
    }

    @Test
    @CardUsed({MythweaverPoq.class, Forest.class, EvolvingWilds.class})
    void conjuresDuplicateAfterEnteringLandIsSacrificed() {
        harness.addToBattlefield(player1, new Forest());
        addCreatureReady(player1, new MythweaverPoq());
        harness.setHand(player1, List.of(new EvolvingWilds()));
        harness.setLibrary(player1, List.of());

        harness.playLand(player1, 0);
        harness.activateAbility(player1, 2, null, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Evolving Wilds")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Evolving Wilds");
    }

    @Test
    void tokenLandDoesNotConsumeOncePerTurnTrigger() {
        harness.addToBattlefield(player1, new Forest());
        addCreatureReady(player1, new MythweaverPoq());
        Card tokenLand = new Forest();
        tokenLand.setToken(true);
        harness.enterBattlefieldAndReturn(player1, tokenLand);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Forest")).isEqualTo(2);

        harness.setHand(player1, List.of(new Plains()));
        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Plains")).isEqualTo(2);
    }

    @Test
    void opponentsLandDoesNotTriggerOrConsumeAllowance() {
        harness.addToBattlefield(player1, new Forest());
        addCreatureReady(player1, new MythweaverPoq());
        harness.enterBattlefieldAndReturn(player2, new Plains());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Plains")).isZero();
        assertThat(countPermanents(player2, "Plains")).isEqualTo(1);

        harness.setHand(player1, List.of(new Plains()));
        harness.playLand(player1, 0);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Plains")).isEqualTo(2);
    }

    @Test
    void triggerResetsOnTheNextTurn() {
        harness.addToBattlefield(player1, new Forest());
        addCreatureReady(player1, new MythweaverPoq());
        harness.setHand(player1, List.of(new Plains()));
        harness.playLand(player1, 0);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Plains")).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new Plains());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Plains")).isEqualTo(4);
    }

    @Test
    void characteristicAbilityWorksInHandAndIgnoresOpponentsLands() {
        Card poq = new MythweaverPoq();
        harness.setHand(player1, List.of(poq));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectiveCardPower(gd, poq)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, poq)).isEqualTo(2);
    }
}
