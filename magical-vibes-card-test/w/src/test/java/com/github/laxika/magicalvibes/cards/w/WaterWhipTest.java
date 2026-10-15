package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaterWhip.class, GrizzlyBears.class, Island.class, SolRing.class, Unsummon.class})
class WaterWhipTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to two creatures and draws two cards")
    void returnsCreaturesAndDrawsTwoCards() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new WaterWhip()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Waterbend taps five creatures while casting")
    void waterbendTapsFiveCreatures() {
        Permanent firstSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent thirdSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fourthSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fifthSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new WaterWhip()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(), false, null, null, null, null, null, false,
                null, null, null, List.of(firstSource.getId(), secondSource.getId(), thirdSource.getId(),
                        fourthSource.getId(), fifthSource.getId()), List.of(), false,
                null, null, List.of(), List.of(), null, null, true);

        assertThat(firstSource.isTapped()).isTrue();
        assertThat(secondSource.isTapped()).isTrue();
        assertThat(thirdSource.isTapped()).isTrue();
        assertThat(fourthSource.isTapped()).isTrue();
        assertThat(fifthSource.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new WaterWhip()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void drawsTwoCardsWithNoTargets() {
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new WaterWhip()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    void returnsOneFriendlyCreatureAndDrawsTwoCards() {
        Card creatureCard = new GrizzlyBears();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, creatureCard);
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new WaterWhip()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creatureCard, firstDraw, secondDraw);
    }

    @Test
    void cannotCastWithoutPayingWaterbendCost() {
        harness.setHand(player1, List.of(new WaterWhip()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void paysWaterbendWithManaAnArtifactAndASummoningSickCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(true);
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new WaterWhip()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(), false, null, null, null, null, null, false,
                null, null, null, List.of(artifact.getId(), creature.getId()), List.of(), false,
                null, null, List.of(), List.of(), null, null, true);

        assertThat(artifact.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    void cannotChooseMoreThanTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaterWhip()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotDrawWhenItsOnlyTargetBecomesIllegal() {
        Card creatureCard = new GrizzlyBears();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, creatureCard);
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new WaterWhip()));
        harness.setHand(player2, List.of(new Unsummon()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of(creature.getId()));
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creatureCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    void returnsRemainingLegalTargetAndDrawsWhenOneTargetLeaves() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        Permanent first = harness.addToBattlefieldAndReturn(player2, firstCard);
        Permanent second = harness.addToBattlefieldAndReturn(player2, secondCard);
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new WaterWhip()));
        harness.setHand(player2, List.of(new Unsummon()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.castAndResolveInstant(player2, 0, first.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
