package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkorpekhLord.class, Ornithopter.class, GrizzlyBears.class})
class SkorpekhLordTest extends BaseCardTest {

    @Test
    void buffsOtherArtifactCreaturesYouControl() {
        harness.addToBattlefield(player1, new SkorpekhLord());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent nonArtifactCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentArtifactCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, artifactCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonArtifactCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nonArtifactCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentArtifactCreature)).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, opponentArtifactCreature, Keyword.MENACE)).isFalse();
    }

    @Test
    void unearthReturnsWithHasteAndExilesAtTheNextEndStep() {
        harness.setGraveyard(player1, List.of(new SkorpekhLord()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent lord = findPermanent(player1, "Skorpekh Lord");
        assertThat(lord.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Skorpekh Lord");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Skorpekh Lord");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Skorpekh Lord"));
    }
}
