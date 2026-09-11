package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExtraArms.class, GrizzlyBears.class, LlanowarElves.class})
class ExtraArmsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with the enchanted creature deals 2 damage to a target player")
    void attackingDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachExtraArms(attacker);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Attacking with the enchanted creature deals 2 damage to a target creature")
    void attackingDealsDamageToCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attachExtraArms(attacker);
        Permanent victim = addCreatureReady(player2, new LlanowarElves());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    private void attachExtraArms(Permanent creature) {
        Permanent aura = new Permanent(new ExtraArms());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }
}
