package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrzaChiefArtificer.class, Memnite.class, GrizzlyBears.class, Bonesplitter.class})
class UrzaChiefArtificerTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifact creatures reduces the generic mana cost")
    void affinityForArtifactCreaturesReducesGenericCost() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Memnite());
        }
        harness.setHand(player1, List.of(new UrzaChiefArtificer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Artifact creatures you control have menace")
    void grantsMenaceToOwnArtifactCreaturesOnly() {
        Permanent urza = harness.addToBattlefieldAndReturn(player1, new UrzaChiefArtificer());
        Permanent ownArtifactCreature = harness.addToBattlefieldAndReturn(player1, new Memnite());
        Permanent ownNonArtifactCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        Permanent opponentArtifactCreature = harness.addToBattlefieldAndReturn(player2, new Memnite());

        assertThat(gqs.hasKeyword(gd, ownArtifactCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownNonArtifactCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownArtifact, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentArtifactCreature, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, urza, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Creates a Construct token at the beginning of your end step with artifact-count power and toughness")
    void createsConstructTokenAtEndStep() {
        harness.addToBattlefield(player1, new UrzaChiefArtificer());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Bonesplitter());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(construct.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(construct.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(construct.getEffectivePower()).isEqualTo(4);
        assertThat(construct.getEffectiveToughness()).isEqualTo(4);
    }
}
