package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SaprazzanBailiffTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by exiling all artifact and enchantment cards from all graveyards")
    void entersExilingArtifactsAndEnchantmentsFromAllGraveyards() {
        Card artifact = new DarksteelRelic();
        Card enchantment = new GloriousAnthem();
        Card creature = new GrizzlyBears();
        Card land = new Mountain();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(artifact, creature));
        gd.playerGraveyards.get(player2.getId()).addAll(List.of(enchantment, land));

        castBailiff();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(creature)
                .doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(land);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(artifact);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(enchantment);
    }

    @Test
    @DisplayName("Returns all artifact and enchantment cards from all graveyards to their owners' hands when it leaves")
    void leavesReturningArtifactsAndEnchantmentsToTheirOwnersHands() {
        harness.addToBattlefield(player1, new SaprazzanBailiff());

        Card artifact = new DarksteelRelic();
        Card enchantment = new GloriousAnthem();
        Card creature = new GrizzlyBears();
        Card land = new Mountain();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(artifact, creature));
        gd.playerGraveyards.get(player2.getId()).addAll(List.of(enchantment, land));

        Permanent bailiff = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SaprazzanBailiff)
                .findFirst()
                .orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bailiff));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerHands.get(player2.getId())).contains(enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(creature)
                .doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(land);
    }

    private void castBailiff() {
        harness.setHand(player1, List.of(new SaprazzanBailiff()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
