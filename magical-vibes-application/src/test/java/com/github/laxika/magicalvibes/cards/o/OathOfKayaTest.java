package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathOfKaya.class, GideonBlackblade.class, GrizzlyBears.class})
class OathOfKayaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 3 damage to the target and gains 3 life")
    void etbDealsDamageAndGainsLife() {
        harness.setHand(player1, List.of(new OathOfKaya()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Gains 2 life and deals 2 damage once when an opponent attacks a planeswalker")
    void triggersOnceForMultipleAttackers() {
        Permanent planeswalker = addGideon(player1);
        harness.addToBattlefield(player1, new OathOfKaya());
        addReadyCreature(player2);
        addReadyCreature(player2);

        declareAttackers(player2, List.of(0, 1), Map.of(0, planeswalker.getId(), 1, planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger when an opponent attacks the player instead")
    void doesNotTriggerForAttackOnPlayer() {
        harness.addToBattlefield(player1, new OathOfKaya());
        addReadyCreature(player2);

        declareAttackers(player2, List.of(0), null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addGideon(Player player) {
        Permanent planeswalker = new Permanent(new GideonBlackblade());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        planeswalker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }

    private void addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }
}
