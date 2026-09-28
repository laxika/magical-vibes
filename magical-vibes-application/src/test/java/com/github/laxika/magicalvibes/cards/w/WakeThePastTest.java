package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WakeThePast.class, CopperMyr.class, IchorWellspring.class, GrizzlyBears.class})
class WakeThePastTest extends BaseCardTest {

    @Test
    void returnsAllOwnArtifactsWithHaste() {
        Card artifactCreature = new CopperMyr();
        Card artifact = new IchorWellspring();
        Card nonartifact = new GrizzlyBears();
        Card opponentArtifact = new IchorWellspring();
        Card wakeThePast = new WakeThePast();
        harness.setGraveyard(player1, List.of(artifactCreature, artifact, nonartifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));
        harness.setHand(player1, List.of(wakeThePast));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(artifactCreature, artifact);
        Permanent returnedArtifactCreature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(artifactCreature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returnedArtifactCreature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(nonartifact.getId(), wakeThePast.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentArtifact);
    }

    @Test
    void hasteExpiresAtEndOfTurn() {
        Card artifactCreature = new CopperMyr();
        Card wakeThePast = new WakeThePast();
        harness.setGraveyard(player1, List.of(artifactCreature));
        harness.setHand(player1, List.of(wakeThePast));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent returnedArtifactCreature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(returnedArtifactCreature.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(returnedArtifactCreature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
