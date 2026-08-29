package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EfficientConstructionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an artifact creates a 1/1 colorless Thopter artifact creature token with flying")
    void artifactSpellCreatesThopter() {
        harness.addToBattlefield(player1, new EfficientConstruction());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(thopter.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
        assertThat(thopter.getCard().getColor()).isNull();
        assertThat(thopter.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        assertThat(thopter.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(thopter.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Casting a nonartifact spell does not create a Thopter")
    void nonartifactSpellDoesNotCreateThopter() {
        harness.addToBattlefield(player1, new EfficientConstruction());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent casting an artifact does not create a Thopter")
    void opponentArtifactSpellDoesNotCreateThopter() {
        harness.addToBattlefield(player1, new EfficientConstruction());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).hasSize(1);
    }
}
