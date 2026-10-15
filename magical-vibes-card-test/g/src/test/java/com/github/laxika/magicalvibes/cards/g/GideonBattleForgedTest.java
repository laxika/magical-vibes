package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.k.KytheonHeroOfAkros;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KytheonHeroOfAkros.class, Shock.class, TimberpackWolf.class})
class GideonBattleForgedTest extends BaseCardTest {

    @Test
    @DisplayName("+2 makes the targeted creature attack Gideon on its controller's next turn")
    void plusTwoForcesTargetedCreatureToAttackGideon() {
        Permanent gideon = addGideon(player1, 3);
        Permanent bear = addCreature(player2, "Bear");

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(gd.creatureMustAttackPermanentNextTurn).containsEntry(bear.getId(), gideon.getId());
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.creatureMustAttackPermanentNextTurn).isEmpty();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int bearIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bear);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(bearIndex),
                Map.of(bearIndex, player1.getId())))
                .isInstanceOf(IllegalStateException.class);

        gs.declareAttackers(gd, player2, List.of(bearIndex), Map.of(bearIndex, gideon.getId()));

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("+2 with no target is legal and forces nothing")
    void plusTwoAllowsNoTarget() {
        Permanent gideon = addGideon(player1, 3);
        addCreature(player2, "Bear");

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.creatureMustAttackPermanentNextTurn).isEmpty();
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+2 can't target a creature its controller controls")
    void plusTwoRejectsOwnCreature() {
        addGideon(player1, 3);
        Permanent ownBear = addCreature(player1, "OwnBear");

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(ownBear.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.creatureMustAttackPermanentNextTurn).isEmpty();
    }

    @Test
    @DisplayName("+1 untaps the target and makes it indestructible until your next turn")
    void plusOneUntapsAndGrantsIndestructible() {
        Permanent gideon = addGideon(player1, 3);
        Permanent bear = addCreature(player1, "Bear");
        bear.tap();

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);

        // A creature card is 2/2 here, so 2 damage would be lethal without indestructible.
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.assertOnBattlefield(player1, "Bear");
    }

    @Test
    @DisplayName("0 animates Gideon into a 4/4 indestructible creature and prevents damage to him")
    void zeroAnimatesAndPreventsDamage() {
        Permanent gideon = addGideon(player1, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, gideon)).isTrue();
        assertThat(gqs.getEffectivePower(gd, gideon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gideon)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, gideon, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, gideon.getId());

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    private Permanent addGideon(Player player, int loyalty) {
        KytheonHeroOfAkros card = new KytheonHeroOfAkros();
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
        return addCreatureReady(player, card);
    }

    @Test
    void plusOneProtectsOpposingCreatureUntilAbilityControllersNextTurn() {
        addGideon(player1, 3);
        Permanent wolf = addCreatureReady(player2, new TimberpackWolf());
        wolf.tap();

        harness.activateAbility(player1, 0, 1, null, wolf.getId());
        harness.passBothPriorities();
        assertThat(wolf.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void zeroAnimationAndDamagePreventionExpireAtCleanup() {
        Permanent gideon = addGideon(player1, 3);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.isCreature(gd, gideon)).isFalse();
        assertThat(gqs.hasKeyword(gd, gideon, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, gideon.getId());
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void plusTwoDoesNotRequireTappedCreatureToAttack() {
        addGideon(player1, 3);
        Permanent wolf = addCreatureReady(player2, new TimberpackWolf());
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(wolf.getId()));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        wolf.tap();
        declareAttackers(player2, List.of());

        assertThat(wolf.isAttacking()).isFalse();
    }
}
