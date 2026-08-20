package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SvellaIceShaperTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a snow Icy Manalith that produces snow mana")
    void createsSnowIcyManalith() {
        Permanent svella = addSvella();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(svella), 0, null, null);
        harness.passBothPriorities();

        Permanent manalith = findPermanent(player1, "Icy Manalith");
        int manalithIndex = gd.playerBattlefields.get(player1.getId()).indexOf(manalith);
        harness.activateAbility(player1, manalithIndex, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Looks at exactly four cards and offers a nonland spell for free")
    void looksAtFourCardsAndCastsSpellForFree() {
        Permanent svella = addSvella();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setLibrary(player1, List.of(
                counsel, new Forest(), new Mountain(), new GrizzlyBears(), new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, battlefieldIndex(svella), 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Counsel of the Soratami", "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        int counselIndex = search.params().cards().indexOf(counsel);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(counselIndex));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size() - handBefore).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("May decline the spell and puts all four cards on the library bottom")
    void mayDeclineFreeCast() {
        Permanent svella = addSvella();
        harness.setLibrary(player1, List.of(
                new CounselOfTheSoratami(), new Forest(), new Mountain(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, battlefieldIndex(svella), 1, null, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Counsel of the Soratami"));
    }

    private Permanent addSvella() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent svella = harness.addToBattlefieldAndReturn(player1, new SvellaIceShaper());
        svella.setSummoningSick(false);
        return svella;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
