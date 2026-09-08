package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncendiarySabotageTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact deals 3 damage to each creature")
    void sacrificesArtifactAndDamagesEachCreature() {
        Permanent artifact = new Permanent(artifact());
        gd.playerBattlefields.get(player1.getId()).add(artifact);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CrawWurm());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CrawWurm());

        harness.setHand(player1, List.of(new IncendiarySabotage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithSacrifice(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Artifact");
        harness.assertInGraveyard(player1, "Incendiary Sabotage");
    }

    @Test
    @DisplayName("Cannot cast without an artifact to sacrifice")
    void cannotCastWithoutArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new IncendiarySabotage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    private Card artifact() {
        Card card = new Card();
        card.setName("Artifact");
        card.setType(CardType.ARTIFACT);
        return card;
    }
}
