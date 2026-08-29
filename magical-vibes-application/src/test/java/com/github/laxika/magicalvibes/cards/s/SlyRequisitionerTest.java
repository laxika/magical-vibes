package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlyRequisitionerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Servo when a nontoken artifact you control is put into a graveyard")
    void createsServoForOwnNontokenArtifact() {
        harness.addToBattlefield(player1, new SlyRequisitioner());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        removeToGraveyard(artifact);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for tokens, opponent artifacts, nonartifacts, or artifacts returned to hand")
    void ignoresNonMatchingPermanents() {
        harness.addToBattlefield(player1, new SlyRequisitioner());

        Card tokenCard = new Spellbook();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent nonartifact = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bouncedArtifact = harness.addToBattlefieldAndReturn(player1, new Memnite());

        removeToGraveyard(token);
        removeToGraveyard(opponentArtifact);
        removeToGraveyard(nonartifact);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bouncedArtifact));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isZero();
    }

    private void removeToGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
