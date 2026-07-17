package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HissingIguanarTest extends BaseCardTest {

    // "Whenever another creature dies, you may have this creature deal 1 damage to
    //  target player or planeswalker."

    /** Player1 shocks the named creature; resolve Shock, its death, then the death trigger. */
    private void killWithShock(UUID targetId) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities(); // resolve Shock -> creature dies -> death trigger onto stack
        harness.passBothPriorities(); // resolve the death trigger (MayEffect prompt)
    }

    @Test
    @DisplayName("Accepting deals 1 damage to the chosen target when another creature dies")
    void acceptingDealsDamage() {
        harness.addToBattlefield(player1, new HissingIguanar());
        harness.addToBattlefield(player1, new GrizzlyBears());

        int p2LifeBefore = gd.getLife(player2.getId());

        killWithShock(harness.getPermanentId(player1, "Grizzly Bears"));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 1);
    }

    @Test
    @DisplayName("Declining the may ability deals no damage")
    void decliningDealsNoDamage() {
        harness.addToBattlefield(player1, new HissingIguanar());
        harness.addToBattlefield(player1, new GrizzlyBears());

        int p2LifeBefore = gd.getLife(player2.getId());

        killWithShock(harness.getPermanentId(player1, "Grizzly Bears"));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
    }

    @Test
    @DisplayName("The source's own death does not trigger the ability (\"another creature\")")
    void ownDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new HissingIguanar());

        int p2LifeBefore = gd.getLife(player2.getId());

        killWithShock(harness.getPermanentId(player1, "Hissing Iguanar"));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
    }
}
