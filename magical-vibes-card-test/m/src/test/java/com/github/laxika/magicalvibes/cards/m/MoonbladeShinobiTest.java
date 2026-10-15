package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoonbladeShinobi.class, UniversalAutomaton.class})
class MoonbladeShinobiTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a flying blue Illusion token when dealing combat damage to a player")
    void createsIllusionTokenOnCombatDamage() {
        Permanent shinobi = addCreatureReady(player1, new MoonbladeShinobi());
        shinobi.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        Permanent illusion = findPermanent(player1, "Illusion");
        assertThat(countPermanents(player1, "Illusion")).isEqualTo(1);
        assertThat(illusion.getCard().getSubtypes()).containsExactly(CardSubtype.ILLUSION);
        assertThat(illusion.isTapped()).isFalse();
        assertThat(illusion.isAttacking()).isFalse();
        assertThat(illusion.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(gqs.getEffectivePower(gd, illusion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, illusion)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, illusion, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not create a token when blocked")
    void blockedShinobiDoesNotCreateToken() {
        Permanent shinobi = addCreatureReady(player1, new MoonbladeShinobi());
        shinobi.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new UniversalAutomaton());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Illusion")).isEmpty();
    }

    @Test
    @DisplayName("Ninjutsu returns an unblocked attacker and puts the Shinobi onto the battlefield attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new UniversalAutomaton());
        addCreatureReady(player2, new UniversalAutomaton());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MoonbladeShinobi()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.assertInHand(player1, "Universal Automaton");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInHand(player1, "Moonblade Shinobi");
        harness.passUntil(TurnStep.COMBAT_DAMAGE);

        harness.assertInHand(player1, "Universal Automaton");
        Permanent shinobi = findPermanent(player1, "Moonblade Shinobi");
        assertThat(shinobi.isTapped()).isTrue();
        assertThat(shinobi.isAttacking()).isTrue();
        assertThat(shinobi.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Each Shinobi creates one token, regardless of the amount of combat damage")
    void multipleShinobiCreateOneTokenEach() {
        addCreatureReady(player1, new MoonbladeShinobi()).setAttacking(true);
        addCreatureReady(player1, new MoonbladeShinobi()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Illusion")).isEqualTo(2);
        assertThat(findPermanents(player2, "Illusion")).isEmpty();
    }

    @Test
    @DisplayName("The attacking controller creates the Illusion")
    void defendingSeatShinobiCreatesTokenForItsController() {
        addCreatureReady(player2, new MoonbladeShinobi()).setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Illusion")).isEqualTo(1);
        assertThat(findPermanents(player1, "Illusion")).isEmpty();
    }

    @Test
    @DisplayName("Ninjutsu cannot return a blocked attacker")
    void ninjutsuRejectsBlockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new UniversalAutomaton());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new UniversalAutomaton());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player1, List.of(new MoonbladeShinobi()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        harness.assertInHand(player1, "Moonblade Shinobi");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Insufficient ninjutsu mana does not return the attacker")
    void ninjutsuRequiresFullManaCost() {
        Permanent attacker = addCreatureReady(player1, new UniversalAutomaton());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player1, List.of(new MoonbladeShinobi()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        harness.assertInHand(player1, "Moonblade Shinobi");
        assertThat(gd.stack).isEmpty();
    }
}
