package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HotshotInvestigators.class, GrizzlyBears.class, Plains.class})
class HotshotInvestigatorsTest extends BaseCardTest {

    @Test
    void returnsCreatureYouControlAndInvestigates() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castHotshot(target.getId());

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void returnsOpponentsCreatureWithoutInvestigating() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castHotshot(target.getId());

        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void canDeclineOptionalTarget() {
        harness.setHand(player1, List.of(new HotshotInvestigators()));
        addCastMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Hotshot Investigators")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new HotshotInvestigators()));
        addCastMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }

    private void castHotshot(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new HotshotInvestigators()));
        addCastMana();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
