package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ParadiseDruid;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KinnanBonderProdigy.class, SolRing.class, ParadiseDruid.class,
        LlanowarElves.class, GrizzlyBears.class, Forest.class, Shock.class})
class KinnanBonderProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a nonland permanent for mana adds one more mana of its type")
    void tappingNonlandPermanentForManaAddsMana() {
        harness.addToBattlefield(player1, new KinnanBonderProdigy());
        Permanent solRing = harness.addToBattlefieldAndReturn(player1, new SolRing());

        harness.activateAbility(player1, 1, null, null);

        assertThat(solRing.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("The bonus follows the color chosen by an any-color permanent")
    void tappingAnyColorPermanentAddsChosenColor() {
        harness.addToBattlefield(player1, new KinnanBonderProdigy());
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new ParadiseDruid());
        druid.setSummoningSick(false);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability offers only non-Human creatures from the top five")
    void offersNonHumanCreature() {
        harness.addToBattlefield(player1, new KinnanBonderProdigy());
        harness.setLibrary(player1, List.of(
                new KinnanBonderProdigy(), new LlanowarElves(), new GrizzlyBears(),
                new Forest(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice search =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(search).isNotNull();
        assertThat(search.allCards().stream().filter(card -> search.validCardIds().contains(card.getId())))
                .extracting(Card::getName)
                .containsExactly("Llanowar Elves", "Grizzly Bears");

        harness.handleMultipleCardsChosen(player1, List.of(search.validCardIds().getFirst()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Kinnan, Bonder Prodigy", "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }
}
