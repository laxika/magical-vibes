package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilundiVision.class, SilundiIsle.class, Divination.class, Forest.class, GrizzlyBears.class, Shock.class})
class SilundiVisionTest extends BaseCardTest {

    @Test
    void visionRevealsAnInstantOrSorceryAndBottomsTheRestRandomly() {
        Shock instant = new Shock();
        GrizzlyBears creature = new GrizzlyBears();
        Forest land = new Forest();
        Divination sorcery = new Divination();
        harness.setLibrary(player1, List.of(instant, creature, land, sorcery));
        harness.setHand(player1, List.of(new SilundiVision()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactlyInAnyOrder(instant, creature, land, sorcery);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(instant.getId(), sorcery.getId());
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(instant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, land, sorcery);
    }

    @Test
    void isleCanBePlayedAsATappedBlueManaSource() {
        SilundiVision card = new SilundiVision();
        harness.setHand(player1, List.of(card));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent isle = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(isle.getCard()).isInstanceOf(SilundiIsle.class);
        assertThat(isle.isTapped()).isTrue();

        isle.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
