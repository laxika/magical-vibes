package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChitteringHost;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrafRats;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MidnightScavengers;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.n.NetworkTerminal;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TouchTheSpiritRealm.class, Forest.class, GrizzlyBears.class, Naturalize.class,
        Ornithopter.class, NetworkTerminal.class, ChitteringHost.class, GrafRats.class,
        MidnightScavengers.class})
class TouchTheSpiritRealmTest extends BaseCardTest {

    @Test
    @DisplayName("ETB optionally exiles an artifact or creature until Touch the Spirit Realm leaves")
    void etbExilesTargetUntilSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TouchTheSpiritRealm()));
        addEnchantmentMana();

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID sourceId = harness.getPermanentId(player1, "Touch the Spirit Realm");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sourceId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Channel exiles an artifact or creature and returns it at the next end step")
    void channelExilesAndReturnsAtNextEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new TouchTheSpiritRealm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.assertNotInHand(player1, "Touch the Spirit Realm");
        harness.assertInGraveyard(player1, "Touch the Spirit Realm");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.passUntil(TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    @Test
    @DisplayName("rejects a land as an ETB target")
    void rejectsLandTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TouchTheSpiritRealm()));
        addEnchantmentMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("rejects a land as a Channel target")
    void rejectsLandChannelTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TouchTheSpiritRealm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB can choose no target even when a creature is present")
    void etbCanChooseNoTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new TouchTheSpiritRealm(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Touch the Spirit Realm");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB does not exile if the enchantment leaves before its trigger resolves")
    void sourceLeavingBeforeTriggerPreventsExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TouchTheSpiritRealm()));
        addEnchantmentMana();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Touch the Spirit Realm"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Touch the Spirit Realm");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB can exile a noncreature artifact")
    void etbExilesNoncreatureArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NetworkTerminal());
        harness.setHand(player1, List.of(new TouchTheSpiritRealm()));
        addEnchantmentMana();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Network Terminal");
    }

    @Test
    @DisplayName("Channel returns a stolen artifact to its owner")
    void channelReturnsToOwner() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.setHand(player1, List.of(new TouchTheSpiritRealm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Network Terminal");
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Network Terminal");
        harness.assertNotOnBattlefield(player1, "Network Terminal");
    }

    @Test
    @DisplayName("Channel used during an end step waits until the next end step")
    void channelDuringEndStepWaitsForNextEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NetworkTerminal());
        harness.passUntil(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new TouchTheSpiritRealm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);
        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Network Terminal");
        harness.passUntil(TurnStep.UPKEEP);
        harness.assertNotOnBattlefield(player2, "Network Terminal");
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player2, "Network Terminal");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Network Terminal");
    }

    @Test
    @DisplayName("Exiling a melded creature until the enchantment leaves returns both component cards")
    void etbReturnsBothMeldComponents() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChitteringHost());
        target.getMeldComponentCards().addAll(List.of(new GrafRats(), new MidnightScavengers()));
        harness.setHand(player1, List.of(new TouchTheSpiritRealm()));
        addEnchantmentMana();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Chittering Host");

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Touch the Spirit Realm"));

        harness.assertOnBattlefield(player2, "Graf Rats");
        harness.assertOnBattlefield(player2, "Midnight Scavengers");
        harness.assertNotOnBattlefield(player2, "Chittering Host");
    }

    private void addEnchantmentMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
