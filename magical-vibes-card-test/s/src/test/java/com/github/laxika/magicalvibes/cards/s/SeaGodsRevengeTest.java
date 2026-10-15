package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VoyagesEnd;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeaGodsRevenge.class, GrizzlyBears.class, GiantSpider.class, AirElemental.class, VoyagesEnd.class})
class SeaGodsRevengeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to three opposing creatures and then scries 1")
    void returnsThreeOpposingCreaturesAndScries() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent spider = addCreatureReady(player2, new GiantSpider());
        Permanent elemental = addCreatureReady(player2, new AirElemental());

        harness.setHand(player1, List.of(new SeaGodsRevenge()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(bears.getId(), spider.getId(), elemental.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Giant Spider");
        harness.assertInHand(player2, "Air Elemental");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertInGraveyard(player1, "Sea God's Revenge");
    }

    @Test
    @DisplayName("Can resolve with no targets")
    void canResolveWithNoTargets() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SeaGodsRevenge()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SeaGodsRevenge()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Still returns a legal target and scries when another target leaves")
    void resolvesWithOneRemainingLegalTarget() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent spider = addCreatureReady(player2, new GiantSpider());
        harness.setHand(player1, List.of(new SeaGodsRevenge()));
        harness.setHand(player2, List.of(new VoyagesEnd()));
        harness.setLibrary(player2, List.of());
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, List.of(bears.getId(), spider.getId()));
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Giant Spider");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.assertInGraveyard(player1, "Sea God's Revenge");
    }

    @Test
    @DisplayName("Does not scry when its only target becomes illegal")
    void doesNotScryWhenAllTargetsBecomeIllegal() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new SeaGodsRevenge()));
        harness.setHand(player2, List.of(new VoyagesEnd()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, List.of(bears.getId()));
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertInGraveyard(player1, "Sea God's Revenge");
    }

    @Test
    @DisplayName("Can put the top card on the bottom after choosing no targets")
    void scriesToBottomWithNoTargets() {
        GrizzlyBears topCard = new GrizzlyBears();
        GiantSpider nextCard = new GiantSpider();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new SeaGodsRevenge()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard, topCard);
        harness.assertInGraveyard(player1, "Sea God's Revenge");
    }

    @Test
    @DisplayName("Cannot choose more than three targets")
    void cannotChooseFourTargets() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GiantSpider());
        Permanent third = addCreatureReady(player2, new AirElemental());
        Permanent fourth = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeaGodsRevenge()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseDuplicateTargets() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeaGodsRevenge()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bears.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
