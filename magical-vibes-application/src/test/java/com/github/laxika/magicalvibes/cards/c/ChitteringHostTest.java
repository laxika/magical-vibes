package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrafRats;
import com.github.laxika.magicalvibes.cards.m.MidnightScavengers;
import com.github.laxika.magicalvibes.cards.s.Smother;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChitteringHost.class, GrafRats.class, MidnightScavengers.class, Smother.class})
class ChitteringHostTest extends BaseCardTest {

    @Test
    void entryAffectsOtherOwnCreaturesPresentAtResolutionOnly() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrafRats());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrafRats());
        Permanent host = harness.enterBattlefieldAndReturn(player1, new ChitteringHost());
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new GrafRats());

        harness.passBothPriorities();
        Permanent later = harness.addToBattlefieldAndReturn(player1, new GrafRats());

        for (Permanent affected : List.of(existing, beforeResolution)) {
            assertThat(affected.getPowerModifier()).isEqualTo(1);
            assertThat(affected.getToughnessModifier()).isZero();
            assertThat(gqs.hasKeyword(gd, affected, Keyword.MENACE)).isTrue();
        }
        for (Permanent unaffected : List.of(opponent, later)) {
            assertThat(unaffected.getPowerModifier()).isZero();
            assertThat(gqs.hasKeyword(gd, unaffected, Keyword.MENACE)).isFalse();
        }
        assertThat(host.getPowerModifier()).isZero();
    }

    @Test
    void entryAbilityStillResolvesAfterHostLeaves() {
        Permanent rats = harness.addToBattlefieldAndReturn(player1, new GrafRats());
        Permanent host = harness.enterBattlefieldAndReturn(player1, new ChitteringHost());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, host));

        harness.passBothPriorities();

        assertThat(rats.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rats, Keyword.MENACE)).isTrue();
    }

    @Test
    void boostAndGrantedMenaceExpireAtEndOfTurn() {
        Permanent rats = harness.addToBattlefieldAndReturn(player1, new GrafRats());
        harness.enterBattlefieldAndReturn(player1, new ChitteringHost());
        harness.passBothPriorities();
        assertThat(rats.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, rats, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(rats.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, rats, Keyword.MENACE)).isFalse();
    }

    @Test
    void newlyEnteredHostCanAttackAndRequiresTwoBlockers() {
        Permanent host = harness.enterBattlefieldAndReturn(player1, new ChitteringHost());
        harness.passBothPriorities();
        harness.addToBattlefield(player2, new MidnightScavengers());
        harness.addToBattlefield(player2, new MidnightScavengers());

        assertThat(host.isSummoningSick()).isTrue();
        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(host.isAttacking()).isTrue();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
    }

    @Test
    @CardUsed({Smother.class})
    void meldedHostCannotBeTargetedBySmother() {
        harness.addToBattlefield(player1, new GrafRats());
        harness.addToBattlefield(player1, new MidnightScavengers());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent host = findPermanent(player1, "Chittering Host");
        harness.setHand(player2, List.of(new Smother()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, host.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 3 or less");
    }

    @Test
    void returningMeldedHostToHandReturnsBothFrontFaces() {
        GrafRats rats = new GrafRats();
        MidnightScavengers scavengers = new MidnightScavengers();
        harness.addToBattlefield(player1, rats);
        harness.addToBattlefield(player1, scavengers);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent host = findPermanent(player1, "Chittering Host");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, host));

        harness.assertNotOnBattlefield(player1, "Chittering Host");
        assertThat(gd.playerHands.get(player1.getId())).contains(rats, scavengers);
        harness.assertNotInHand(player1, "Chittering Host");
    }
}
