package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VoidStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating tucks both Void Stalker and the target creature into their owners' libraries")
    void tucksBothCreatures() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new VoidStalker());
        stalker.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Void Stalker");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName)).contains("Void Stalker");
        assertThat(gd.playerDecks.get(player2.getId()).stream().map(Card::getName)).contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Targeting Void Stalker itself just puts it into its owner's library")
    void canTargetItself() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new VoidStalker());
        stalker.setSummoningSick(false);

        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, stalker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Void Stalker");
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName).filter("Void Stalker"::equals))
                .hasSize(1);
    }

    @Test
    @DisplayName("Ability cannot be activated without paying {2}{U}")
    void requiresMana() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new VoidStalker());
        stalker.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
