package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlimmerLens.class, GrizzlyBears.class})
class GlimmerLensTest extends BaseCardTest {

    @Test
    @DisplayName("For Mirrodin! creates and attaches a 2/2 Rebel")
    void forMirrodinCreatesAndAttachesRebel() {
        harness.setHand(player1, List.of(new GlimmerLens()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent lens = findPermanent(player1, "Glimmer Lens");
        Permanent rebel = findPermanent(player1, "Rebel");
        assertThat(rebel.getCard().isToken()).isTrue();
        assertThat(rebel.getCard().getPower()).isEqualTo(2);
        assertThat(rebel.getCard().getToughness()).isEqualTo(2);
        assertThat(lens.getAttachedTo()).isEqualTo(rebel.getId());
    }

    @Test
    @DisplayName("Draws when the equipped creature attacks with another creature")
    void drawsWhenEquippedCreatureAttacksWithAnotherCreature() {
        harness.setHand(player1, List.of());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lens = harness.addToBattlefieldAndReturn(player1, new GlimmerLens());
        lens.setAttachedTo(equippedCreature.getId());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(equippedCreature),
                gd.playerBattlefields.get(player1.getId()).indexOf(otherCreature)));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when the equipped creature attacks alone")
    void doesNotDrawWhenEquippedCreatureAttacksAlone() {
        harness.setHand(player1, List.of());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lens = harness.addToBattlefieldAndReturn(player1, new GlimmerLens());
        lens.setAttachedTo(equippedCreature.getId());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(equippedCreature)));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }
}
