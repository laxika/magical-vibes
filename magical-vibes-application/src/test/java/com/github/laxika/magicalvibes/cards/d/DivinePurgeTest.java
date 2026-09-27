package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DivinePurge.class, GrizzlyBears.class, Memnite.class, HillGiant.class})
class DivinePurgeTest extends BaseCardTest {

    @Test
    void exilesSmallArtifactsAndCreaturesButNotLargerCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent memnite = harness.addToBattlefieldAndReturn(player2, new Memnite());
        harness.addToBattlefield(player2, new HillGiant());

        castPurge();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Memnite");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.findExiledCard(bears.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(memnite.getOriginalCard().getId())).isNotNull();
    }

    @Test
    void exiledPermanentCostsTwoMoreAndEntersTapped() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castPurge();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castFromExile(player2, bears.getOriginalCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castFromExile(player2, bears.getOriginalCard().getId());
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
    }

    private void castPurge() {
        harness.setHand(player1, List.of(new DivinePurge()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
