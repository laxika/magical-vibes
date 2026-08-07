package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JaceTelepathUnboundTest extends BaseCardTest {

    @Test
    @DisplayName("+1 shrinks the target by -2/-0 and the shrink outlasts end-of-turn cleanup")
    void plusOneShrinkOutlastsTheTurn() {
        Permanent jace = addJace(player1, 5);
        Permanent bear = addCreature(player2, "Bear");

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gqs.getEffectivePower(gd, bear)).isZero();
    }

    @Test
    @DisplayName("+1 may be activated with no target")
    void plusOneAllowsNoTarget() {
        Permanent jace = addJace(player1, 5);
        addCreature(player2, "Bear");

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-3 lets the targeted instant be cast from the graveyard later that turn, exiling it after")
    void minusThreeGrantsGraveyardCastThatExiles() {
        Permanent jace = addJace(player1, 5);
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        harness.activateAbility(player1, 0, 1, null, shock.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        // The permission does not cast it — the card is still in the graveyard.
        harness.assertInGraveyard(player1, "Shock");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("-9 emblem mills the chosen opponent five cards whenever its controller casts a spell")
    void minusNineEmblemMillsOnSpellCast() {
        addJace(player1, 9);
        harness.setLibrary(player2, List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    private Permanent addJace(Player player, int loyalty) {
        JaceVrynsProdigy card = new JaceVrynsProdigy();
        Permanent perm = new Permanent(card);
        perm.setTransformed(true);
        perm.setCard(card.getBackFaceCard());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addCreature(Player player, String name) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
