package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Fissure;
import com.github.laxika.magicalvibes.cards.w.WitchHunter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Lurker.class, Fissure.class, WitchHunter.class})
class LurkerTest extends BaseCardTest {

    @Test
    void cannotBeTargetedBeforeAttackingOrBlocking() {
        Permanent lurker = addCreatureReady(player2, new Lurker());
        harness.setHand(player1, List.of(new Fissure()));
        addFissureMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, lurker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of spells");
    }

    @Test
    void canBeTargetedAfterAttackingThisTurn() {
        Permanent lurker = addCreatureReady(player1, new Lurker());
        harness.setHand(player1, List.of(new Fissure()));
        addFissureMana(player1);

        declareAttackers(List.of(0));
        harness.castAndResolveInstant(player1, 0, lurker.getId());

        harness.assertInGraveyard(player1, "Fissure");
        harness.assertInGraveyard(player1, "Lurker");
    }

    @Test
    void canBeTargetedAfterBlockingThisTurn() {
        addCreatureReady(player1, new WitchHunter());
        Permanent lurker = addCreatureReady(player2, new Lurker());
        harness.setHand(player1, List.of(new Fissure()));
        addFissureMana(player1);

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.castAndResolveInstant(player1, 0, lurker.getId());

        harness.assertInGraveyard(player1, "Fissure");
        harness.assertInGraveyard(player2, "Lurker");
    }

    @Test
    void targetingRestrictionReturnsAtStartOfNextTurn() {
        Permanent lurker = addCreatureReady(player1, new Lurker());

        declareAttackers(List.of(0));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player2, List.of(new Fissure()));
        addFissureMana(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, lurker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of spells");
    }

    @Test
    void activatedAbilitiesCanTargetBeforeAttackingOrBlocking() {
        Permanent hunter = addCreatureReady(player1, new WitchHunter());
        Permanent lurker = addCreatureReady(player2, new Lurker());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, lurker.getId());
        harness.passBothPriorities();

        assertThat(hunter.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Lurker");
        harness.assertInHand(player2, "Lurker");
    }

    private void addFissureMana(Player player) {
        harness.addMana(player, ManaColor.RED, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }
}
