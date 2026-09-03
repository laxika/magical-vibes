package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VenomspoutBrackus.class, WallOfAir.class, GrizzlyBears.class})
class VenomspoutBrackusTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to an attacking creature with flying")
    void damagesAttackingFlyer() {
        Permanent brackus = addReady(player1, new VenomspoutBrackus());
        Permanent attacker = addReady(player2, new WallOfAir());
        attacker.setAttacking(true);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(brackus.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Wall of Air");
    }

    @Test
    @DisplayName("Deals 5 damage to a blocking creature with flying")
    void damagesBlockingFlyer() {
        addReady(player1, new VenomspoutBrackus());
        Permanent blocker = addReady(player2, new WallOfAir());
        blocker.setBlocking(true);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Wall of Air");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        addReady(player1, new VenomspoutBrackus());
        Permanent attacker = addReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Cannot target a flyer that is neither attacking nor blocking")
    void cannotTargetNonCombatFlyer() {
        addReady(player1, new VenomspoutBrackus());
        Permanent flyer = addReady(player2, new WallOfAir());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new VenomspoutBrackus()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent brackus = findPermanent(player1, "Venomspout Brackus");
        assertThat(brackus.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(brackus));
        harness.passBothPriorities();

        assertThat(brackus.isFaceDown()).isFalse();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
