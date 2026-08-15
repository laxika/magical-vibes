package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.n.NissaGenesisMage;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeployTheGatewatchTest extends BaseCardTest {

    @Test
    @DisplayName("puts up to two planeswalkers from the top seven onto the battlefield")
    void putsUpToTwoPlaneswalkersOntoBattlefield() {
        Card jace = new JaceBeleren();
        Card nissa = new NissaGenesisMage();
        Card chandra = new ChandraNalaar();
        Card shock = new Shock();
        setLibrary(jace, nissa, chandra, shock);

        castDeployTheGatewatch();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                jace.getId(), nissa.getId(), chandra.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(jace.getId(), nissa.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .containsExactlyInAnyOrder(jace, nissa);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(chandra, shock);
    }

    @Test
    @DisplayName("may decline and puts non-planeswalkers back on the bottom")
    void mayDeclineAndBottomsNonPlaneswalkers() {
        Card jace = new JaceBeleren();
        Card shock = new Shock();
        setLibrary(jace, shock);

        castDeployTheGatewatch();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(jace, shock);
    }

    private void castDeployTheGatewatch() {
        harness.setHand(player1, List.of(new DeployTheGatewatch()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
