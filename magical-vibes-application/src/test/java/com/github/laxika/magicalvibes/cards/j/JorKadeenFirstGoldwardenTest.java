package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SwiftfootBoots;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JorKadeenFirstGoldwardenTest extends BaseCardTest {

    @Test
    void getsBoostForEachEquippedCreatureAndDrawsAtFourPower() {
        Permanent jor = addCreatureReady(player1, new JorKadeenFirstGoldwarden());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachEquipment(jor);
        attachEquipment(bears);
        Card drawn = new GrizzlyBears();
        setDeck(drawn);

        attackWith(jor);

        assertThat(gqs.getEffectivePower(gd, jor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, jor)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void doesNotDrawWhenTheBoostLeavesJorBelowFourPower() {
        Permanent jor = addCreatureReady(player1, new JorKadeenFirstGoldwarden());
        attachEquipment(jor);
        setDeck(new GrizzlyBears());

        attackWith(jor);

        assertThat(gqs.getEffectivePower(gd, jor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, jor)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void unattachedEquipmentDoesNotCount() {
        Permanent jor = addCreatureReady(player1, new JorKadeenFirstGoldwarden());
        harness.addToBattlefield(player1, new SwiftfootBoots());
        setDeck(new GrizzlyBears());

        attackWith(jor);

        assertThat(gqs.getEffectivePower(gd, jor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, jor)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void attackWith(Permanent creature) {
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.passBothPriorities();
    }

    private void attachEquipment(Permanent creature) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SwiftfootBoots());
        equipment.setAttachedTo(creature.getId());
    }

    private void setDeck(Card card) {
        gd.playerHands.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(card);
    }
}
