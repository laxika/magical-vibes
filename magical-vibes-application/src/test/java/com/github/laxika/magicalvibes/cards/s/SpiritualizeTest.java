package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Chainflinger;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Spiritualize.class, Chainflinger.class, Forest.class})
class SpiritualizeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and gains life equal to damage dealt by the targeted creature")
    void drawsAndGainsLifeFromTargetedCreatureDamage() {
        Permanent chainflinger = addCreatureReady(player2, new Chainflinger());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.setHand(player1, List.of(new Spiritualize()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0, chainflinger.getId());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The delayed damage trigger wears off at end of turn")
    void triggerWearsOffAtEndOfTurn() {
        Permanent chainflinger = addCreatureReady(player1, new Chainflinger());

        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Spiritualize()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0, chainflinger.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setLife(player1, 20);
        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Gains life when the targeted creature deals noncombat damage")
    void gainsLifeFromNoncombatDamage() {
        Permanent chainflinger = addCreatureReady(player2, new Chainflinger());

        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Spiritualize()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0, chainflinger.getId());

        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Gains life when the targeted creature deals combat damage before dying")
    void gainsLifeFromTargetedCreatureThatDiesInCombat() {
        Permanent chainflinger = addCreatureReady(player1, new Chainflinger());
        Permanent blocker = addCreatureReady(player2, new Chainflinger());

        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Spiritualize()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0, chainflinger.getId());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(chainflinger))));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        harness.assertInGraveyard(player1, "Chainflinger");
        harness.assertInGraveyard(player2, "Chainflinger");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        UUID forestId = harness.addToBattlefieldAndReturn(player1, new Forest()).getId();

        harness.setHand(player1, List.of(new Spiritualize()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }
}
