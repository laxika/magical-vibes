package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({JayasFirenado.class, AirElemental.class, NicolBolasPlaneswalker.class, Plains.class})
class JayasFirenadoTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a creature, then scries 1")
    void dealsDamageToCreatureAndScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castFirenado(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);

        completeScry();

        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Jaya's Firenado");
    }

    @Test
    @DisplayName("Deals 5 damage to a planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        target.setCounterCount(CounterType.LOYALTY, 7);

        castFirenado(target);

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        completeScry();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Rejects a land target")
    void rejectsLandTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new JayasFirenado()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    @DisplayName("Scry can keep the top card without changing either library")
    void keepsTopCard() {
        Plains top = new Plains();
        JayasFirenado second = new JayasFirenado();
        Plains opponentTop = new Plains();
        harness.setLibrary(player1, List.of(top, second));
        harness.setLibrary(player2, List.of(opponentTop));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castFirenado(target);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(top);
        completeScry();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
    }

    @Test
    @DisplayName("Scry can put the top card on the bottom")
    void putsTopCardOnBottom() {
        Plains top = new Plains();
        JayasFirenado second = new JayasFirenado();
        harness.setLibrary(player1, List.of(top, second));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castFirenado(target);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
        harness.assertInGraveyard(player1, "Jaya's Firenado");
    }

    @Test
    @DisplayName("An illegal sole target prevents scrying")
    void doesNotScryWhenTargetLeavesBattlefield() {
        Plains top = new Plains();
        harness.setLibrary(player1, List.of(top));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new JayasFirenado()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.assertInGraveyard(player1, "Jaya's Firenado");
    }

    @Test
    @DisplayName("An empty library does not stop damage or spell resolution")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castFirenado(target);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Jaya's Firenado");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster")
    void damagesOwnCreatureAndScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        castFirenado(target);
        completeScry();

        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Rejects a player target")
    void rejectsPlayerTarget() {
        harness.setHand(player1, List.of(new JayasFirenado()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFirenado(Permanent target) {
        harness.setHand(player1, List.of(new JayasFirenado()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void completeScry() {
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }
}
