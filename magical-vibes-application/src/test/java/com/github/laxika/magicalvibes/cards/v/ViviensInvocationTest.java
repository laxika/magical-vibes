package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class ViviensInvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a chosen creature from the top seven onto the battlefield and it deals its power to the target")
    void putsCreatureAndDealsPowerDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Card chosen = new GrizzlyBears();
        setLibrary(chosen, new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        castInvocation(target);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(chosen.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == chosen)
                .findFirst()
                .orElseThrow();
        assertThat(entered.getMarkedDamage()).isEqualTo(0);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6).doesNotContain(chosen);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May decline to put a creature onto the battlefield")
    void mayDeclineCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Card chosen = new GrizzlyBears();
        setLibrary(chosen, new Shock(), new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        castInvocation(target);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard() == chosen);
        assertThat(target.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castInvocation(Permanent target) {
        harness.setHand(player1, List.of(new ViviensInvocation()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
