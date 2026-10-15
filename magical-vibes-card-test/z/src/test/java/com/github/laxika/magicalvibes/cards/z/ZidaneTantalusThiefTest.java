package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Insurrection;
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

@CardUsed({ZidaneTantalusThief.class, ZealousConscripts.class, GrizzlyBears.class, Insurrection.class})
class ZidaneTantalusThiefTest extends BaseCardTest {

    @Test
    @DisplayName("ETB steals, untaps and grants lifelink and haste to an opponent's creature")
    void etbStealsUntapsAndGrantsKeywords() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ZidaneTantalusThief()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Creates a Treasure when an opponent gains control of a permanent from you")
    void opponentGainingControlCreatesTreasure() {
        harness.addToBattlefieldAndReturn(player1, new ZidaneTantalusThief());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ZealousConscripts()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castCreature(player2, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Returning the stolen creature at cleanup creates a Treasure and ends both keyword grants")
    void returningCreatureCreatesTreasure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castZidane(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.CLEANUP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("The stolen creature can attack immediately and gains life for its new controller")
    void stolenCreatureAttacksWithLifelink() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castZidane(target);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(target)));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Stealing Zidane itself creates a Treasure for its previous controller")
    void stealingZidaneCreatesTreasureForPreviousController() {
        Permanent zidane = harness.addToBattlefieldAndReturn(player1, new ZidaneTantalusThief());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ZealousConscripts()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castCreature(player2, 0, 0, zidane.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(zidane);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Simultaneously stealing Zidane and another creature creates one Treasure for each")
    void simultaneousTheftIncludesEveryPermanent() {
        Permanent zidane = harness.addToBattlefieldAndReturn(player1, new ZidaneTantalusThief());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new Insurrection(), "{5}{R}{R}{R}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(zidane, other);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    private void castZidane(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ZidaneTantalusThief()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();
    }
}
