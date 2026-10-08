package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PsychicSurgery;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoidStalker.class, GrizzlyBears.class, PsychicSurgery.class, Unsummon.class})
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

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new VoidStalker());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, stalker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new VoidStalker());
        stalker.setSummoningSick(false);
        stalker.tap();
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, stalker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetStillMovesWhenSourceIsReturnedToHand() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new VoidStalker());
        stalker.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoidStalker());
        harness.setHand(player2, java.util.List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, stalker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Void Stalker");
        harness.assertNotOnBattlefield(player2, "Void Stalker");
        assertThat(gd.playerDecks.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(stalker.getCard());
    }

    @Test
    void illegalTargetPreventsSourceFromMoving() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new VoidStalker());
        stalker.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoidStalker());
        harness.setHand(player2, java.util.List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Void Stalker");
        harness.assertInHand(player2, "Void Stalker");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(stalker.getCard());
    }

    @Test
    void sharedOwnerShufflesOnlyOnce() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new VoidStalker());
        stalker.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VoidStalker());
        harness.addToBattlefield(player2, new PsychicSurgery());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).contains(stalker.getCard(), target.getCard());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(PsychicSurgery.class);
    }
}
