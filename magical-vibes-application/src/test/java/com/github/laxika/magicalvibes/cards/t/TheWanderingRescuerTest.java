package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheWanderingRescuer.class, GrizzlyBears.class, Shock.class})
class TheWanderingRescuerTest extends BaseCardTest {

    @Test
    @DisplayName("Grants hexproof to other tapped creatures you control")
    void grantsHexproofToOtherTappedCreaturesYouControl() {
        Permanent rescuer = addCreatureReady(player1, new TheWanderingRescuer());
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        tappedCreature.tap();
        Permanent untappedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.tap();
        rescuer.tap();

        assertThat(gqs.hasKeyword(gd, tappedCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, untappedCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, rescuer, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Hexproof follows a creature's tap state")
    void hexproofFollowsTapState() {
        harness.addToBattlefield(player1, new TheWanderingRescuer());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();

        creature.tap();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();

        creature.untap();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Tapped creatures with granted hexproof cannot be targeted by opponents")
    void tappedCreatureCannotBeTargetedByOpponent() {
        harness.addToBattlefield(player1, new TheWanderingRescuer());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }
}
