package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RepelTheAbominableTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from non-Human creatures")
    void preventsCombatDamageFromNonHumanCreatures() {
        harness.setLife(player1, 20);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, createCreature("Elf", false));
        attacker.setSummoningSick(false);
        castRepelTheAbominable();

        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent combat damage from Human creatures")
    void doesNotPreventCombatDamageFromHumanCreatures() {
        harness.setLife(player1, 20);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, createCreature("Human", true));
        attacker.setSummoningSick(false);
        castRepelTheAbominable();

        attacker.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents damage from non-Human spells")
    void preventsDamageFromNonHumanSpells() {
        harness.setLife(player1, 20);
        castRepelTheAbominable();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Clears non-Human source prevention at end of turn")
    void clearsAtEndOfTurn() {
        castRepelTheAbominable();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.preventAllDamageFromNonHumanSources).isFalse();
    }

    private static Card createCreature(String name, boolean human) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        if (human) {
            card.setSubtypes(List.of(CardSubtype.HUMAN));
        }
        return card;
    }

    private void castRepelTheAbominable() {
        harness.setHand(player1, List.of(new RepelTheAbominable()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);
    }
}
