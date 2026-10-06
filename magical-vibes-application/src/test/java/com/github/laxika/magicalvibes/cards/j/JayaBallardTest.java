package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.s.ShivanFire;
import com.github.laxika.magicalvibes.cards.w.WarlordsFury;
import com.github.laxika.magicalvibes.cards.r.Remand;
import com.github.laxika.magicalvibes.cards.k.KeldonRaider;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({JayaBallard.class, KeldonRaider.class, ShivanFire.class, WarlordsFury.class})
class JayaBallardTest extends BaseCardTest {

    @Test
    @DisplayName("+1 mana ability adds 3 restricted red mana")
    void plusOneManaAbilityAddsRestrictedMana() {
        Permanent jaya = addReadyJaya(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(jaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(3);
    }


    @Test
    @DisplayName("+1 rummage: discard 2, draw 2")
    void plusOneRummageDiscardTwoDraw() {
        Permanent jaya = addReadyJaya(player1);
        Card cardA = new KeldonRaider();
        Card cardB = new KeldonRaider();
        Card cardC = new ShivanFire();
        harness.setHand(player1, List.of(cardA, cardB, cardC));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Should be awaiting X value choice for how many to discard
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class) != null).isTrue();

        // Choose to discard 2
        harness.handleXValueChosen(player1, 2);

        // Should be awaiting discard choice
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null).isTrue();

        // Discard first card
        harness.handleCardChosen(player1, 0);

        // Should be awaiting second discard
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null).isTrue();

        // Discard second card
        harness.handleCardChosen(player1, 0);

        // Hand should have 1 original card + 2 drawn cards = 3 cards
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        // Graveyard should have 2 discarded cards
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        // Loyalty should be 5 + 1 = 6
        assertThat(jaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("+1 rummage: choose 0, no discard or draw")
    void plusOneRummageChooseZero() {
        addReadyJaya(player1);
        Card cardA = new KeldonRaider();
        harness.setHand(player1, List.of(cardA));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class) != null).isTrue();

        // Choose to discard 0
        harness.handleXValueChosen(player1, 0);

        // Hand should be unchanged (1 card)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Graveyard should be empty
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("+1 rummage: empty hand does nothing")
    void plusOneRummageEmptyHand() {
        addReadyJaya(player1);
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Should NOT be awaiting any input — effect should resolve immediately
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }


    @Test
    void emblemCastsSorceryAndExilesItAfterJayaLeaves() {
        createEmblem();
        WarlordsFury spell = new WarlordsFury();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new KeldonRaider()));
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void emblemCastsInstantAndExilesItAfterResolution() {
        createEmblem();
        ShivanFire spell = new ShivanFire();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KeldonRaider());
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromGraveyardTargeting(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @CardUsed({Remand.class})
    void emblemSpellCounteredByRemandReturnsToHand() {
        createEmblem();
        WarlordsFury spell = new WarlordsFury();
        harness.setHand(player1, List.of(new Remand()));
        harness.setLibrary(player1, List.of(new KeldonRaider()));
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromGraveyard(player1, 0);
        harness.castInstant(player1, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
    }

    @Test
    void jayaManaPaysForSorceryCastUsingEmblem() {
        createEmblem();
        addReadyJaya(player1);
        WarlordsFury spell = new WarlordsFury();
        harness.setLibrary(player1, List.of(new KeldonRaider()));
        harness.setGraveyard(player1, List.of(spell));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void manaCanPayForSorceryFromHandButNotCreature() {
        addReadyJaya(player1);
        harness.setHand(player1, List.of(new KeldonRaider(), new WarlordsFury()));
        harness.setLibrary(player1, List.of(new KeldonRaider()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.RED)).isZero();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0)).isInstanceOf(IllegalStateException.class);
        harness.castSorcery(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void rummageDiscardsAtMostThreeAndDrawsOnlyAfterAllDiscards() {
        addReadyJaya(player1);
        Card first = new KeldonRaider();
        Card second = new KeldonRaider();
        Card third = new ShivanFire();
        Card kept = new WarlordsFury();
        Card drawn = new KeldonRaider();
        harness.setHand(player1, List.of(first, second, third, kept));
        harness.setLibrary(player1, List.of(drawn, new KeldonRaider(), new ShivanFire()));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleXValueChosen(player1, 4)).isInstanceOf(IllegalArgumentException.class);
        harness.handleXValueChosen(player1, 3);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4).contains(kept, drawn);
    }

    private void createEmblem() {
        Permanent jaya = addReadyJaya(player1);
        jaya.setCounterCount(CounterType.LOYALTY, 8);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(jaya);
        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot activate -8 with only 5 loyalty")
    void cannotActivateUltimateWithInsufficientLoyalty() {
        addReadyJaya(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }


    private Permanent addReadyJaya(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new JayaBallard());
        perm.setCounterCount(CounterType.LOYALTY, 5);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
