package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BarbaraWright;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LupariShield;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KarvanistaLoyalLupari.class, LupariShield.class, BarbaraWright.class, GrizzlyBears.class})
class KarvanistaLoyalLupariTest extends BaseCardTest {

    @Test
    void attackingPutsACounterOnEachHumanYouControl() {
        Permanent karvanista = addCreatureReady(player1, new KarvanistaLoyalLupari());
        Permanent human = addCreatureReady(player1, new BarbaraWright());
        Permanent nonHuman = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(karvanista.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(human.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonHuman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void adventureGrantsIndestructibleToYourHumansUntilYourNextTurn() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new BarbaraWright());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentHuman = harness.addToBattlefieldAndReturn(player2, new BarbaraWright());
        KarvanistaLoyalLupari card = new KarvanistaLoyalLupari();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonHuman, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentHuman, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        KarvanistaLoyalLupari card = new KarvanistaLoyalLupari();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Karvanista, Loyal Lupari");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void attackTriggerUsesHumansControlledAtResolutionEvenAfterKarvanistaLeaves() {
        Permanent karvanista = addCreatureReady(player1, new KarvanistaLoyalLupari());
        Permanent opponentHuman = addCreatureReady(player2, new BarbaraWright());

        declareAttackers(player1, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(karvanista);
        gd.playerGraveyards.get(player1.getId()).add(karvanista.getCard());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new BarbaraWright());
        resolveAllTriggers();

        assertThat(human.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentHuman.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void adventureProtectionExpiresOnCastersNextTurnAfterHumanChangesController() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new BarbaraWright());
        harness.setHand(player1, List.of(new KarvanistaLoyalLupari()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.UPKEEP);
        gd.playerBattlefields.get(player1.getId()).remove(human);
        gd.playerBattlefields.get(player2.getId()).add(human);
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void adventureDoesNotProtectHumansEnteringAfterResolution() {
        KarvanistaLoyalLupari card = new KarvanistaLoyalLupari();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        Permanent lateHuman = harness.addToBattlefieldAndReturn(player1, new BarbaraWright());

        assertThat(gqs.hasKeyword(gd, lateHuman, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }
}
