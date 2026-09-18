package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FuneralPyre;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CabalTherapy.class, SuntailHawk.class, FuneralPyre.class, KrosanVerge.class})
class CabalTherapyTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses a nonland name and discards every matching card from the target's hand")
    void discardsEveryMatchingCard() {
        Card firstHawk = new SuntailHawk();
        Card secondHawk = new SuntailHawk();
        Card funeralPyre = new FuneralPyre();
        Card krosanVerge = new KrosanVerge();
        castFromHand(new ArrayList<>(List.of(firstHawk, secondHawk, funeralPyre, krosanVerge)));

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("Suntail Hawk", "Funeral Pyre").doesNotContain("Krosan Verge");

        harness.handleListChoice(player1, "Suntail Hawk");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(funeralPyre, krosanVerge);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(firstHawk, secondHawk);
    }

    @Test
    @DisplayName("Does not discard cards when the chosen name is absent from the target's hand")
    void absentNameDoesNothing() {
        Card funeralPyre = new FuneralPyre();
        Card krosanVerge = new KrosanVerge();
        harness.setHand(player1, List.of(new CabalTherapy(), new SuntailHawk()));
        harness.setHand(player2, new ArrayList<>(List.of(funeralPyre, krosanVerge)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Suntail Hawk");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(funeralPyre, krosanVerge);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target its controller and discard matching cards from that hand")
    void canTargetController() {
        CabalTherapy therapy = new CabalTherapy();
        Card firstHawk = new SuntailHawk();
        Card secondHawk = new SuntailHawk();
        Card funeralPyre = new FuneralPyre();
        Card krosanVerge = new KrosanVerge();
        harness.setHand(player1, new ArrayList<>(List.of(therapy, firstHawk, secondHawk, funeralPyre, krosanVerge)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Suntail Hawk");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(funeralPyre, krosanVerge);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(therapy, firstHawk, secondHawk);
    }

    @Test
    @DisplayName("Cannot target a nonplayer permanent")
    void cannotTargetPermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());
        harness.setHand(player1, List.of(new CabalTherapy()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Flashback sacrifices a creature and exiles Cabal Therapy after it resolves")
    void flashbackSacrificesCreatureAndExilesSpell() {
        Permanent creature = addCreatureReady(player1, new SuntailHawk());
        CabalTherapy therapy = new CabalTherapy();
        harness.setGraveyard(player1, List.of(therapy));
        harness.setHand(player2, List.of(new FuneralPyre()));

        harness.castFlashbackWithSacrifice(player1, 0, player2.getId(), creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Funeral Pyre");

        harness.assertNotInGraveyard(player1, "Cabal Therapy");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Cabal Therapy"));
    }

    @Test
    @DisplayName("Flashback requires sacrificing a creature")
    void flashbackRejectsNonCreatureSacrifice() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());
        CabalTherapy therapy = new CabalTherapy();
        harness.setGraveyard(player1, List.of(therapy));

        assertThatThrownBy(() -> harness.castFlashbackWithSacrifice(player1, 0, player2.getId(), land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(therapy);
    }

    private void castFromHand(List<Card> targetHand) {
        harness.setHand(player1, List.of(new CabalTherapy()));
        harness.setHand(player2, targetHand);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
