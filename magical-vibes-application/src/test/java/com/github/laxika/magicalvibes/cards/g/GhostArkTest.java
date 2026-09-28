package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostArk.class, GrizzlyBears.class, Ornithopter.class, MindStone.class})
class GhostArkTest extends BaseCardTest {

    @Test
    void grantsUnearthToArtifactCreatureCardsOnly() {
        CardSetup cards = setUpBattlefieldAndGraveyard();

        crewGhostArk(cards.ark());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateGraveyardAbility(player1, 2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(cards.artifactCreature().getId()));
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unearthGrantEndsAtEndOfTurn() {
        CardSetup cards = setUpBattlefieldAndGraveyard();

        crewGhostArk(cards.ark());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 2))
                .isInstanceOf(IllegalStateException.class);
    }

    private CardSetup setUpBattlefieldAndGraveyard() {
        Ornithopter artifactCreature = new Ornithopter();
        GrizzlyBears nonArtifactCreature = new GrizzlyBears();
        MindStone artifact = new MindStone();
        harness.setGraveyard(player1, List.of(nonArtifactCreature, artifact, artifactCreature));

        Permanent ark = addReady(new GhostArk());
        addReady(new GrizzlyBears());
        return new CardSetup(artifactCreature, ark);
    }

    private void crewGhostArk(Permanent ark) {
        int arkIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ark);
        harness.activateAbility(player1, arkIndex, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private record CardSetup(Ornithopter artifactCreature, Permanent ark) {
    }
}
