package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.s.SilverKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RainOfBlades.class, GrizzlyBears.class, FugitiveWizard.class, HowlingMine.class, SilverKnight.class})
class RainOfBladesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each attacking creature")
    void deals1DamageToEachAttackingCreature() {
        harness.forceActivePlayer(player1);
        Permanent a1 = addAttacker(player1, player2, new GrizzlyBears());
        Permanent a2 = addAttacker(player1, player2, new GrizzlyBears());
        castRainOfBlades(player2);

        assertThat(a1.getMarkedDamage()).isEqualTo(1);
        assertThat(a2.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Kills 1-toughness attacking creatures")
    void killsOneToughnessAttackers() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2, new FugitiveWizard());
        castRainOfBlades(player2);

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertInGraveyard(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Does not damage non-attacking creatures")
    void doesNotDamageNonAttackers() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2, new GrizzlyBears());
        Permanent idle = addCreatureReady(player1, new GrizzlyBears());
        castRainOfBlades(player2);

        assertThat(idle.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not damage attacking noncreature permanents")
    void doesNotDamageAttackingNoncreatures() {
        harness.forceActivePlayer(player1);
        Permanent artifact = addAttacker(player1, player2, new HowlingMine());
        castRainOfBlades(player2);

        assertThat(artifact.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Checks which creatures are attacking when it resolves")
    void checksAttackingStatusAtResolution() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttacker(player1, player2, new GrizzlyBears());
        castRainOfBladesWithoutResolving(player2);

        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
    }

    private void castRainOfBlades(Player caster) {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castFromHand(caster, new RainOfBlades(), "{W}");
        harness.passBothPriorities();
    }

    private void castRainOfBladesWithoutResolving(Player caster) {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castFromHand(caster, new RainOfBlades(), "{W}");
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent perm = addCreatureReady(controller, card);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }

    @Test
    @DisplayName("Damages every attacking creature regardless of its controller")
    void damagesAttackingCreaturesOnBothBattlefields() {
        harness.forceActivePlayer(player1);
        Permanent player1Attacker = addAttacker(player1, player2, new SilverKnight());
        Permanent player2Attacker = addAttacker(player2, player1, new SilverKnight());

        castRainOfBlades(player2);

        assertThat(player1Attacker.getMarkedDamage()).isEqualTo(1);
        assertThat(player2Attacker.getMarkedDamage()).isEqualTo(1);
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
