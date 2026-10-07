package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.Levitation;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.StormfrontPegasus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TectonicRift.class, Forest.class, RuneclawBear.class, StormfrontPegasus.class,
        Levitation.class, Naturalize.class})
class TectonicRiftTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys the target land")
    void destroysTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new TectonicRift()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Creatures without flying can't block this turn, fliers are unaffected")
    void nonFliersCantBlock() {
        harness.addToBattlefield(player2, new Forest());
        Permanent ownBears = addCreatureReady(player1, new RuneclawBear());
        Permanent oppBears = addCreatureReady(player2, new RuneclawBear());
        Permanent oppHawk = addCreatureReady(player2, new StormfrontPegasus());

        harness.setHand(player1, List.of(new TectonicRift()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(bls.canBlockAttacker(gd, ownBears, oppBears,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, oppBears, ownBears,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, oppHawk, ownBears,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bears = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new TectonicRift()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy a land controlled by the caster")
    void destroysOwnLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new TectonicRift()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, land.getId());

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("An illegal land target prevents the blocking restriction from taking effect")
    void illegalLandTargetPreventsAllEffects() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new TectonicRift()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, land.getId());

        gd.playerBattlefields.get(player2.getId()).remove(land);
        harness.setGraveyard(player2, List.of(land.getCard()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tectonic Rift");
        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Nonflying creatures entering after resolution also can't block")
    void affectsCreaturesEnteringLater() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new TectonicRift()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, land.getId());

        Permanent blocker = harness.enterBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent flyingBlocker = harness.enterBattlefieldAndReturn(player2, new StormfrontPegasus());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyingBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Gaining and losing flying after resolution changes whether a creature can block")
    void respondsToFlyingChanges() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent blocker = addCreatureReady(player1, new RuneclawBear());
        Permanent attacker = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new TectonicRift(), new Levitation(), new Naturalize()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, land.getId());
        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player1.getId()))).isFalse();

        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player1.getId()))).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Levitation"));
        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("Blocking restriction expires at the end of the turn")
    void restrictionExpiresAtEndOfTurn() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new TectonicRift()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, land.getId());
        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
