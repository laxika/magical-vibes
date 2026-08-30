package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NullmageAdvocate.class, Bonesplitter.class, GrizzlyBears.class, LightningBolt.class, Shock.class})
class NullmageAdvocateTest extends BaseCardTest {

    @Test
    void returnsTwoCardsFromOpponentsGraveyardAndDestroysArtifact() {
        Permanent advocate = addReadyAdvocate();
        Card first = new LightningBolt();
        Card second = new Shock();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        harness.setGraveyard(player2, List.of(first, second));

        harness.activateAbilityWithMultiTargets(player1, index(advocate), 0,
                List.of(first.getId(), second.getId(), artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId)
                .contains(first.getId(), second.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(artifact.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(artifact.getCard().getId()));
        assertThat(advocate.isTapped()).isTrue();
    }

    @Test
    void rejectsCreatureAsTheDestructionTarget() {
        Permanent advocate = addReadyAdvocate();
        Card first = new LightningBolt();
        Card second = new Shock();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(first, second));

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, index(advocate), 0,
                List.of(first.getId(), second.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAdvocate() {
        Permanent advocate = new Permanent(new NullmageAdvocate());
        advocate.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(advocate);
        return advocate;
    }

    private int index(Permanent advocate) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(advocate);
    }
}
