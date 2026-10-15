package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DepthshakerTitan.class, AccordersShield.class, Memnite.class, GrizzlyBears.class})
class DepthshakerTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Animates any number of targeted artifacts and sacrifices them at the next end step")
    void animatesAndSacrificesTargetedArtifacts() {
        Permanent firstShield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent secondShield = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent opponentShield = harness.addToBattlefieldAndReturn(player2, new AccordersShield());

        castTitan(List.of(firstShield.getId(), secondShield.getId()));

        assertThat(gqs.isCreature(gd, firstShield)).isTrue();
        assertThat(gqs.getEffectivePower(gd, firstShield)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstShield)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, secondShield)).isTrue();
        assertThat(gqs.isCreature(gd, opponentShield)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            gs.advanceStep(gd);
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(firstShield.getId())
                        || permanent.getId().equals(secondShield.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Accorder's Shield", "Accorder's Shield");
    }

    @Test
    @DisplayName("Gives artifact creatures you control melee, trample, and haste")
    void grantsKeywordsToOwnArtifactCreatures() {
        Permanent ownMemnite = harness.addToBattlefieldAndReturn(player1, new Memnite());
        Permanent opponentMemnite = harness.addToBattlefieldAndReturn(player2, new Memnite());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castTitan(List.of());

        Permanent titan = findPermanent(player1, "Depthshaker Titan");
        for (Keyword keyword : List.of(Keyword.MELEE, Keyword.TRAMPLE, Keyword.HASTE)) {
            assertThat(gqs.hasKeyword(gd, titan, keyword)).isTrue();
            assertThat(gqs.hasKeyword(gd, ownMemnite, keyword)).isTrue();
            assertThat(gqs.hasKeyword(gd, opponentMemnite, keyword)).isFalse();
            assertThat(gqs.hasKeyword(gd, ownBear, keyword)).isFalse();
        }
    }

    @Test
    @DisplayName("Rejects a creature and an opponent's artifact as targets")
    void rejectsIllegalTargets() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new Memnite());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new AccordersShield());

        prepareCast();
        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature artifact");

        prepareCast();
        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(opponentArtifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    private void castTitan(List<java.util.UUID> targetIds) {
        prepareCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new DepthshakerTitan()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
