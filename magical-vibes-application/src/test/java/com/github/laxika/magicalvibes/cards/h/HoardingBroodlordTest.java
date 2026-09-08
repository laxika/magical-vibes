package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HoardingBroodlord.class, GrizzlyBears.class, Plains.class, Shock.class})
class HoardingBroodlordTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield ability exiles one library card face down with it")
    void exilesOneLibraryCardFaceDownWithSource() {
        HoardingBroodlord broodlord = new HoardingBroodlord();
        List<Card> library = List.of(new GrizzlyBears(), new Plains());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(broodlord));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(broodlord.getId()))
                .findFirst()
                .orElseThrow();
        ExiledCardEntry entry = gd.findExiledCard(library.getFirst().getId());
        assertThat(entry).isNotNull();
        assertThat(entry.sourcePermanentId()).isEqualTo(source.getId());
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.getLast());
    }

    @Test
    @DisplayName("It lets you cast the source-linked exiled spell with convoke")
    void castsSourceLinkedExiledSpellWithConvoke() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HoardingBroodlord());
        Card exiledCard = new GrizzlyBears();
        gd.addToExile(player1.getId(), exiledCard, source.getId(), true);
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExileWithConvoke(player1, exiledCard.getId(), List.of(convoker.getId()));

        assertThat(convoker.isTapped()).isTrue();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(exiledCard.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(exiledCard.getId()));
    }

    @Test
    @DisplayName("The exile-only convoke grant does not apply to spells cast from hand")
    void doesNotGrantConvokeToSpellsFromHand() {
        harness.addToBattlefieldAndReturn(player1, new HoardingBroodlord());
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstantWithConvoke(
                player1, 0, List.of(), List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(convoker.isTapped()).isFalse();
    }
}
