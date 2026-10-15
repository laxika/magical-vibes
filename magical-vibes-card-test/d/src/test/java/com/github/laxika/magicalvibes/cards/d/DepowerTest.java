package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Depower.class, Forest.class, GrizzlyBears.class})
class DepowerTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target creature -4/-0 until end of turn and draws a card")
    void weakensTargetCreatureAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.setHand(player1, List.of(new Depower()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(-2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Costs only {U} when targeting an attacking creature")
    void costsTwoLessWhenTargetingAttackingCreature() {
        Permanent attacker = addAttacker(player2, player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Depower()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, attacker.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Requires the full cost when targeting a non-attacking creature")
    void requiresFullCostWhenTargetingNonAttackingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Depower()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new Depower()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The power reduction expires at the end of the turn")
    void powerReductionExpires() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Depower()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(-2);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not draw when its only target leaves before resolution")
    void doesNotDrawWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new Depower()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player1, "Depower");
    }

    @Test
    @DisplayName("Still resolves if the discounted target stops attacking")
    void resolvesAfterTargetStopsAttacking() {
        Permanent target = addAttacker(player2, player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Depower()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());

        target.setAttacking(false);
        target.setAttackTarget(null);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(-2);
        harness.assertInHand(player1, "Forest");
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(controller, card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        permanent.setAttackTarget(defender.getId());
        return permanent;
    }
}
