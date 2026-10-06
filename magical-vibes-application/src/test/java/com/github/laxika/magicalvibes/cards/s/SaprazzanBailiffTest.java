package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GlistenerElf;
import com.github.laxika.magicalvibes.cards.f.ForcedWorship;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaprazzanBailiff.class, DarksteelRelic.class, ForcedWorship.class,
        GlistenerElf.class, Mountain.class})
class SaprazzanBailiffTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by exiling all artifact and enchantment cards from all graveyards")
    void entersExilingArtifactsAndEnchantmentsFromAllGraveyards() {
        Card artifact = new DarksteelRelic();
        Card enchantment = new ForcedWorship();
        Card creature = new GlistenerElf();
        Card land = new Mountain();
        harness.setGraveyard(player1, List.of(artifact, creature));
        harness.setGraveyard(player2, List.of(enchantment, land));

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
        Card artifact = new DarksteelRelic();
        Card enchantment = new ForcedWorship();
        Card creature = new GlistenerElf();
        Card land = new Mountain();
        harness.setGraveyard(player1, List.of(artifact, creature));
        harness.setGraveyard(player2, List.of(enchantment, land));

        Permanent bailiff = harness.addToBattlefieldAndReturn(player1, new SaprazzanBailiff());
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

    @Test
    @DisplayName("Returns matching cards when it leaves the battlefield for exile")
    void leavesReturningArtifactsAndEnchantmentsWhenExiled() {
        Card artifact = new DarksteelRelic();
        Card enchantment = new ForcedWorship();
        Card creature = new GlistenerElf();
        Card land = new Mountain();
        harness.setGraveyard(player1, List.of(artifact, creature));
        harness.setGraveyard(player2, List.of(enchantment, land));

        Permanent bailiff = harness.addToBattlefieldAndReturn(player1, new SaprazzanBailiff());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, bailiff));

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
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(bailiff.getCard());
    }

    @Test
    @DisplayName("Leaving returns newly buried cards but does not return cards exiled on entry")
    void leavesReturningOnlyCardsCurrentlyInGraveyards() {
        Card exiledArtifact = new DarksteelRelic();
        Card exiledEnchantment = new ForcedWorship();
        harness.setGraveyard(player1, List.of(exiledArtifact));
        harness.setGraveyard(player2, List.of(exiledEnchantment));
        castBailiff();

        Card newArtifact = new DarksteelRelic();
        Card newEnchantment = new ForcedWorship();
        harness.setGraveyard(player1, List.of(newArtifact));
        harness.setGraveyard(player2, List.of(newEnchantment));
        Permanent bailiff = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bailiff));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(newArtifact).doesNotContain(exiledArtifact);
        assertThat(gd.playerHands.get(player2.getId())).contains(newEnchantment).doesNotContain(exiledEnchantment);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiledArtifact);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiledEnchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bailiff.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Leaving before the entry trigger resolves returns cards first and entry still exiles later cards")
    void leavesBeforeEntryTriggerResolves() {
        Card artifact = new DarksteelRelic();
        Card enchantment = new ForcedWorship();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setGraveyard(player2, List.of(enchantment));
        harness.castFromHand(player1, new SaprazzanBailiff(), "{3}{U}{U}");
        harness.passBothPriorities();

        Permanent bailiff = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bailiff));
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerHands.get(player2.getId())).contains(enchantment);

        Card laterArtifact = new DarksteelRelic();
        harness.setGraveyard(player2, List.of(laterArtifact));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(laterArtifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerHands.get(player2.getId())).contains(enchantment).doesNotContain(laterArtifact);
    }

    private void castBailiff() {
        harness.castFromHand(player1, new SaprazzanBailiff(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
