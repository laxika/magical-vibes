package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JodahTheUnifier.class, AdelizTheCinderWind.class, GrizzlyBears.class,
        HillGiant.class, IsamaruHoundOfKonda.class, LlanowarElves.class})
class JodahTheUnifierTest extends BaseCardTest {

    @Test
    @DisplayName("Legendary creatures you control get +X/+X, including Jodah")
    void boostsLegendaryCreaturesByLegendaryCreatureCount() {
        Permanent jodah = harness.addToBattlefieldAndReturn(player1, new JodahTheUnifier());
        Permanent isamaru = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, jodah)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, jodah)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, isamaru)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, isamaru)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a legendary spell from hand cascades into a legendary nonland card")
    void castsLegendarySpellFromHandAndFindsLegendaryCard() {
        prepareCasterTurn();
        harness.addToBattlefield(player1, new JodahTheUnifier());

        LlanowarElves belowHit = new LlanowarElves();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new HillGiant(), new GrizzlyBears(), new IsamaruHoundOfKonda(), belowHit));

        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Isamaru, Hound of Konda");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowHit);
    }

    @Test
    @DisplayName("Nonlegendary spells do not trigger Jodah")
    void nonlegendarySpellDoesNotTrigger() {
        prepareCasterTurn();
        harness.addToBattlefield(player1, new JodahTheUnifier());

        LlanowarElves libraryCard = new LlanowarElves();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(libraryCard);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    private void prepareCasterTurn() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
    }
}
