package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VitoThornOfTheDuskRose.class, AngelOfMercy.class, GrizzlyBears.class})
class VitoThornOfTheDuskRoseTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent loses life equal to life gained")
    void opponentLosesLifeEqualToLifeGained() {
        harness.addToBattlefield(player1, new VitoThornOfTheDuskRose());

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Activated ability grants lifelink to all your creatures until end of turn")
    void grantsLifelinkUntilEndOfTurn() {
        harness.addToBattlefield(player1, new VitoThornOfTheDuskRose());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent vito = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vito, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.LIFELINK)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThat(gqs.hasKeyword(gd, vito, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Opponent gaining life does not trigger Vito")
    void opponentLifeGainDoesNotTrigger() {
        harness.addToBattlefield(player1, new VitoThornOfTheDuskRose());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain lifelink")
    void laterCreaturesDoNotGainLifelink() {
        harness.addToBattlefield(player1, new VitoThornOfTheDuskRose());
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The activated ability still grants lifelink when Vito leaves before resolution")
    void abilityResolvesWithoutVito() {
        harness.addToBattlefield(player1, new VitoThornOfTheDuskRose());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        Permanent vito = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(vito.getCard());

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Vito does not gain lifelink if an opponent controls him when the ability resolves")
    void changedControllerBeforeResolutionDoesNotGrantVitoLifelink() {
        harness.addToBattlefield(player1, new VitoThornOfTheDuskRose());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        Permanent vito = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerBattlefields.get(player2.getId()).add(vito);

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, vito, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Granted lifelink gains life and triggers Vito after combat damage")
    void lifelinkCombatDamageTriggersLifeLoss() {
        harness.addToBattlefield(player1, new VitoThornOfTheDuskRose());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("A life-gain trigger resolves even if Vito leaves the battlefield")
    void lifeGainTriggerResolvesWithoutVito() {
        harness.addToBattlefield(player1, new VitoThornOfTheDuskRose());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent vito = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(vito.getCard());

        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }
}
