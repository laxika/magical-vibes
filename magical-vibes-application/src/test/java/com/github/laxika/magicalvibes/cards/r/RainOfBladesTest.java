package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RainOfBladesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each attacking creature")
    void deals1DamageToEachAttackingCreature() {
        harness.forceActivePlayer(player1);
        Permanent a1 = addAttacker(player1, player2, makeCreature("Bear", 2, 2));
        Permanent a2 = addAttacker(player1, player2, makeCreature("Bear", 2, 2));
        castRainOfBlades();

        assertThat(a1.getMarkedDamage()).isEqualTo(1);
        assertThat(a2.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Kills 1-toughness attacking creatures")
    void killsOneToughnessAttackers() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2, makeCreature("Goblin", 2, 1));
        castRainOfBlades();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Goblin"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Goblin"));
    }

    @Test
    @DisplayName("Does not damage non-attacking creatures")
    void doesNotDamageNonAttackers() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2, makeCreature("Bear", 2, 2));
        Permanent idle = new Permanent(makeCreature("Wall", 0, 4));
        idle.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(idle);
        castRainOfBlades();

        assertThat(idle.getMarkedDamage()).isZero();
    }

    // ===== Helpers =====

    private void castRainOfBlades() {
        harness.setHand(player2, List.of(new RainOfBlades()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(controller.getId()).add(perm);
        return perm;
    }

    private Card makeCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
