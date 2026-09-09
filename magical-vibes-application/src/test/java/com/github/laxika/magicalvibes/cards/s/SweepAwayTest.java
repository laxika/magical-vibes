package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SweepAway.class, GrizzlyBears.class, Forest.class})
class SweepAwayTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a non-attacking creature to its owner's hand")
    void returnsNonAttackingCreatureToHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSweepAway(target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Puts an attacking creature on top of its owner's library")
    void putsAttackingCreatureOnTopOfLibrary() {
        Permanent target = addAttacker();
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        castSweepAway(target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library).hasSize(deckBefore + 1);
        assertThat(library.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Returns the creature to hand if it stops attacking before resolution")
    void returnsToHandIfTargetStopsAttacking() {
        Permanent target = addAttacker();

        castSweepAway(target.getId());
        target.setAttacking(false);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new SweepAway()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSweepAway(UUID targetId) {
        harness.setHand(player1, List.of(new SweepAway()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetId);
    }

    private Permanent addAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
