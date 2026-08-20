package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AetherHelixTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target permanent and a target permanent card from a graveyard to their owners' hands")
    void returnsPermanentAndGraveyardCard() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new AetherHelix()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, graveyardCard.getId(), List.of(permanent.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(graveyardCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(graveyardCard.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(card -> card.getId().equals(permanent.getCard().getId()));
        harness.assertInGraveyard(player1, "Aether Helix");
    }

    @Test
    @DisplayName("Cannot cast without both required targets")
    void requiresBothTargets() {
        Card graveyardCard = new HolyDay();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new AetherHelix()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, graveyardCard.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-permanent card in a graveyard")
    void cannotTargetNonPermanentCardFromGraveyard() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card graveyardCard = new HolyDay();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new AetherHelix()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, graveyardCard.getId(), List.of(permanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
