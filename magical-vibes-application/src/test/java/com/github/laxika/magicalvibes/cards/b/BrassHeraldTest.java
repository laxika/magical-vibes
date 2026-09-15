package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.e.ElvishPromenade;
import com.github.laxika.magicalvibes.cards.i.Index;
import com.github.laxika.magicalvibes.cards.u.UrborgElf;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrassHerald.class, Dodecapod.class, ElvishPromenade.class, Index.class,
        UrborgElf.class, WoodlandChangeling.class, YavimayaCoast.class})
class BrassHeraldTest extends BaseCardTest {

    private void finishAnyReorder() {
        var reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        if (reorder != null) {
            harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(IntStream.range(0, reorder.cards().size()).boxed().toList()));
        }
    }

    private void castHeraldAndChoose(String subtype) {
        harness.castFromHand(player1, new BrassHerald(), "{6}");
        harness.passBothPriorities();          // resolve Brass Herald -> subtype choice pends
        harness.handleListChoice(player1, subtype); // choose type -> reveal trigger queued
        harness.passBothPriorities();          // resolve the reveal trigger
    }

    @Test
    @DisplayName("Creature cards of the chosen type go to hand, the rest to the bottom")
    void revealPutsChosenTypeCreaturesInHand() {
        Card elf1 = new UrborgElf();
        Card elf2 = new UrborgElf();
        Card coast = new YavimayaCoast();
        Card index = new Index();

        harness.setLibrary(player1, List.of(elf1, elf2, coast, index));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        castHeraldAndChoose("ELF");
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(elf1, elf2);
        assertThat(deck).containsExactlyInAnyOrder(coast, index);
    }

    @Test
    @DisplayName("A creature of a different type is not put into hand")
    void revealSkipsOtherCreatureTypes() {
        Card elf = new UrborgElf();
        Card golem = new Dodecapod();
        Card coast = new YavimayaCoast();
        Card index = new Index();

        harness.setLibrary(player1, List.of(elf, golem, coast, index));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        castHeraldAndChoose("ELF");
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(elf);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(golem, coast, index);
        assertThat(deck).containsExactlyInAnyOrder(golem, coast, index);
    }

    @Test
    @DisplayName("Changeling creature cards count as the chosen type")
    void revealChangelingCountsAsChosenType() {
        Card changeling = new WoodlandChangeling();
        Card coast = new YavimayaCoast();
        Card golem = new Dodecapod();
        Card index = new Index();

        harness.setLibrary(player1, List.of(changeling, coast, golem, index));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        castHeraldAndChoose("ELF");
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(changeling);
        assertThat(deck).containsExactlyInAnyOrder(coast, golem, index);
    }

    @Test
    @DisplayName("A noncreature card with the chosen type stays on the bottom")
    void revealSkipsNonCreatureChosenTypeCards() {
        Card elf = new UrborgElf();
        Card elfSorcery = new ElvishPromenade();
        Card coast = new YavimayaCoast();
        Card index = new Index();

        harness.setLibrary(player1, List.of(elf, elfSorcery, coast, index));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        castHeraldAndChoose("ELF");
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elf);
        assertThat(deck).containsExactlyInAnyOrder(elfSorcery, coast, index);
    }

    @Test
    @DisplayName("Only the top four cards are revealed")
    void onlyTopFourAreRevealed() {
        Card elf = new UrborgElf();
        Card nonElf1 = new Dodecapod();
        Card nonElf2 = new YavimayaCoast();
        Card nonElf3 = new Index();
        Card deepElf = new UrborgElf();

        harness.setLibrary(player1, List.of(elf, nonElf1, nonElf2, nonElf3, deepElf));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        castHeraldAndChoose("ELF");
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(elf);
        assertThat(deck).containsExactlyInAnyOrder(nonElf1, nonElf2, nonElf3, deepElf);
    }

    @Test
    @DisplayName("An empty library produces no cards and no reorder interaction")
    void emptyLibraryDoesNotCreateReorderInteraction() {
        harness.setLibrary(player1, List.of());

        castHeraldAndChoose("ELF");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Creatures you control of the chosen type get +1/+1")
    void boostsOwnCreaturesOfChosenType() {
        Permanent elfPerm = harness.addToBattlefieldAndReturn(player1, new UrborgElf());

        Permanent herald = harness.addToBattlefieldAndReturn(player1, new BrassHerald());
        herald.setChosenSubtype(CardSubtype.ELF);

        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's creatures of the chosen type also get +1/+1")
    void boostsOpponentCreaturesOfChosenType() {
        Permanent elfPerm = harness.addToBattlefieldAndReturn(player2, new UrborgElf());

        Permanent herald = harness.addToBattlefieldAndReturn(player1, new BrassHerald());
        herald.setChosenSubtype(CardSubtype.ELF);

        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures of a different type are not boosted")
    void doesNotBoostOtherTypes() {
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new Dodecapod());

        Permanent herald = harness.addToBattlefieldAndReturn(player1, new BrassHerald());
        herald.setChosenSubtype(CardSubtype.ELF);

        var bonus = gqs.computeStaticBonus(gd, golem);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Brass Herald boosts itself when it chooses its own creature type")
    void boostsItselfWhenChoosingGolem() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new BrassHerald());
        herald.setChosenSubtype(CardSubtype.GOLEM);

        var bonus = gqs.computeStaticBonus(gd, herald);
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost disappears when Brass Herald leaves the battlefield")
    void boostRemovedWhenHeraldLeaves() {
        Permanent elfPerm = harness.addToBattlefieldAndReturn(player1, new UrborgElf());

        Permanent herald = harness.addToBattlefieldAndReturn(player1, new BrassHerald());
        herald.setChosenSubtype(CardSubtype.ELF);

        assertThat(gqs.computeStaticBonus(gd, elfPerm).power()).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(herald);
        assertThat(gqs.computeStaticBonus(gd, elfPerm).power()).isEqualTo(0);
    }
}
