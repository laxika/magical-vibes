package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerpentineCurve;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchmageEmeritus.class, BarkshellBlessing.class, GiantGrowth.class,
        GrizzlyBears.class, SerpentineCurve.class})
class ArchmageEmeritusTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant with Archmage Emeritus draws a card")
    void castingInstantDrawsCard() {
        addCreatureReady(player1, new ArchmageEmeritus());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        GiantGrowth drawnCard = new GiantGrowth();
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Copying an instant with Archmage Emeritus draws a card")
    void copyingInstantDrawsCard() {
        addCreatureReady(player1, new ArchmageEmeritus());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        GiantGrowth firstDrawnCard = new GiantGrowth();
        GrizzlyBears secondDrawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.setLibrary(player1, List.of(firstDrawnCard, secondDrawnCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(),
                List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        for (int i = 0; i < 6 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(firstDrawnCard, secondDrawnCard);
    }

    @Test
    @DisplayName("Casting a sorcery draws before the spell resolves")
    void castingSorceryDrawsCard() {
        addCreatureReady(player1, new ArchmageEmeritus());
        ArchmageEmeritus drawnCard = new ArchmageEmeritus();
        harness.setHand(player1, List.of(new SerpentineCurve()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's instant does not trigger magecraft")
    void opponentInstantDoesNotDrawCard() {
        Permanent target = addCreatureReady(player1, new ArchmageEmeritus());
        ArchmageEmeritus libraryCard = new ArchmageEmeritus();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting a creature does not trigger magecraft")
    void creatureSpellDoesNotDrawCard() {
        addCreatureReady(player1, new ArchmageEmeritus());
        ArchmageEmeritus libraryCard = new ArchmageEmeritus();
        harness.setHand(player1, List.of(new ArchmageEmeritus()));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Archmage triggers independently for the same instant")
    void multipleArchmagesEachDrawCard() {
        Permanent target = addCreatureReady(player1, new ArchmageEmeritus());
        addCreatureReady(player1, new ArchmageEmeritus());
        ArchmageEmeritus firstDrawnCard = new ArchmageEmeritus();
        ArchmageEmeritus secondDrawnCard = new ArchmageEmeritus();
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setLibrary(player1, List.of(firstDrawnCard, secondDrawnCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDrawnCard);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDrawnCard, secondDrawnCard);
        assertThat(gd.stack).hasSize(1);
    }
}
