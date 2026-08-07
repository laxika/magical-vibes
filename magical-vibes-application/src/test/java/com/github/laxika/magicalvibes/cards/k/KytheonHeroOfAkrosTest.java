package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KytheonHeroOfAkrosTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking together with two other creatures exiles Kytheon and returns him transformed")
    void transformsWhenThreeCreaturesAttack() {
        Permanent kytheon = addKytheon(player1);
        addCreature(player1, "Ally1");
        addCreature(player1, "Ally2");

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kytheon, Hero of Akros");
        harness.assertOnBattlefield(player1, "Gideon, Battle-Forged");

        Permanent gideon = findPermanent(player1, "Gideon, Battle-Forged");
        assertThat(gideon.isTransformed()).isTrue();
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isPositive();
    }

    @Test
    @DisplayName("Attacking with only one other creature leaves Kytheon untransformed")
    void doesNotTransformWithTwoAttackers() {
        addKytheon(player1);
        addCreature(player1, "Ally1");

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kytheon, Hero of Akros");
        harness.assertNotOnBattlefield(player1, "Gideon, Battle-Forged");
    }

    @Test
    @DisplayName("{2}{W} makes Kytheon indestructible, so lethal damage does not kill him")
    void indestructibleAbilitySurvivesLethalDamage() {
        addKytheon(player1);

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent kytheon = findPermanent(player1, "Kytheon, Hero of Akros");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, kytheon.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kytheon, Hero of Akros");
    }

    @Test
    @DisplayName("Without the indestructible ability, lethal damage kills Kytheon")
    void diesToLethalDamageWithoutActivation() {
        Permanent kytheon = addKytheon(player1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, kytheon.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kytheon, Hero of Akros");
    }

    private Permanent addKytheon(Player player) {
        Permanent perm = new Permanent(new KytheonHeroOfAkros());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void addCreature(Player player, String name) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }

}
