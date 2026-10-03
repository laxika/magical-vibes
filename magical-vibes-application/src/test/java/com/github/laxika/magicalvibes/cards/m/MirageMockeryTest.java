package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirageMockery.class, Ornithopter.class, GrizzlyBears.class})
class MirageMockeryTest extends BaseCardTest {

    @Test
    void createsArtifactCreatureTokenCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        cast(new int[]{0}, List.of(target.getId()), false);

        List<Permanent> tokens = tokens();
        assertThat(tokens).hasSize(1);
        assertThat(gqs.isArtifact(gd, tokens.get(0))).isTrue();
        assertThat(gqs.isCreature(gd, tokens.get(0))).isTrue();
    }

    @Test
    void createsNonartifactCreatureTokenCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(new int[]{1}, List.of(target.getId()), false);

        List<Permanent> tokens = tokens();
        assertThat(tokens).hasSize(1);
        assertThat(gqs.isArtifact(gd, tokens.get(0))).isFalse();
        assertThat(gqs.isCreature(gd, tokens.get(0))).isTrue();
    }

    @Test
    void entwineCreatesBothTokenCopiesAndPaysAdditionalCost() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent nonartifactCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(new int[]{0, 1}, List.of(artifactCreature.getId(), nonartifactCreature.getId()), true);

        List<Permanent> tokens = tokens();
        assertThat(tokens).hasSize(2);
        assertThat(tokens.stream().filter(token -> gqs.isArtifact(gd, token))).hasSize(1);
        assertThat(tokens.stream().filter(token -> !gqs.isArtifact(gd, token))).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void selectedModeRejectsTheOtherCreatureType() {
        Permanent nonartifactCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareMana(false);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(nonartifactCreature.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds, boolean entwined) {
        prepareMana(entwined);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targetIds, null);
        harness.passBothPriorities();
    }

    private void prepareMana(boolean entwined) {
        harness.setHand(player1, List.of(new MirageMockery()));
        harness.addMana(player1, ManaColor.BLUE, entwined ? 2 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, entwined ? 4 : 2);
    }

    private List<Permanent> tokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
