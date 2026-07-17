package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObeliskOfUndoingTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a permanent you own and control to your hand")
    void bouncesOwnedAndControlledPermanent() {
        Permanent obelisk = new Permanent(new ObeliskOfUndoing());
        gd.playerBattlefields.get(player1.getId()).add(obelisk);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(obelisk.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a permanent you control but do not own")
    void cannotTargetControlledButNotOwned() {
        Permanent obelisk = new Permanent(new ObeliskOfUndoing());
        gd.playerBattlefields.get(player1.getId()).add(obelisk);

        // A creature player1 controls but player2 owns (stolen).
        Permanent stolen = new Permanent(new GrizzlyBears());
        stolen.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(stolen);
        gd.stolenCreatures.put(stolen.getId(), player2.getId());

        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, stolen.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent you own but do not control")
    void cannotTargetOwnedButNotControlled() {
        Permanent obelisk = new Permanent(new ObeliskOfUndoing());
        gd.playerBattlefields.get(player1.getId()).add(obelisk);

        // A creature player1 owns but player2 controls (stolen by player2).
        Permanent lent = new Permanent(new GrizzlyBears());
        lent.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(lent);
        gd.stolenCreatures.put(lent.getId(), player1.getId());

        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, lent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
