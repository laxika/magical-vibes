package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherHelix.class, GrizzlyBears.class, HolyDay.class, Forest.class})
class AetherHelixTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target permanent to its owner's hand and a permanent card from your graveyard to your hand")
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
        Card graveyardCard = new GrizzlyBears();
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

    @Test
    void cannotTargetOpponentsGraveyard() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));
        prepareHelix();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, graveyardCard.getId(), List.of(permanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresGraveyardTargetEvenWithLegalPermanentTarget() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareHelix();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, null, List.of(permanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canReturnOwnPermanent() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCard));
        prepareHelix();

        harness.castSorcery(player1, 0, graveyardCard.getId(), List.of(permanent.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .extracting(Card::getId).containsExactlyInAnyOrder(permanent.getCard().getId(), graveyardCard.getId());
    }

    @Test
    void stillReturnsGraveyardCardWhenPermanentTargetLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCard));
        prepareHelix();
        harness.castSorcery(player1, 0, graveyardCard.getId(), List.of(permanent.getId()));
        harness.getGameData().playerBattlefields.get(player2.getId()).remove(permanent);
        harness.setGraveyard(player2, List.of(permanent.getCard()));

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    @Test
    void stillBouncesPermanentWhenGraveyardTargetLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCard));
        prepareHelix();
        harness.castSorcery(player1, 0, graveyardCard.getId(), List.of(permanent.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(graveyardCard));

        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Aether Helix");
    }

    @Test
    void canReturnLandsFromBothZones() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new Forest());
        Card graveyardCard = new Forest();
        harness.setGraveyard(player1, List.of(graveyardCard));
        prepareHelix();

        harness.castSorcery(player1, 0, graveyardCard.getId(), List.of(permanent.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertInHand(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    void bouncesPermanentBeforeReturningGraveyardCard() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new Forest());
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCard));
        prepareHelix();

        harness.castSorcery(player1, 0, graveyardCard.getId(), List.of(permanent.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText).toList())
                .containsSubsequence("Forest is returned to its owner's hand.",
                        gd.playerIdToName.get(player1.getId()) + " returns Grizzly Bears from graveyard to hand.");
    }

    @Test
    void resolvesNeitherReturnWhenBothTargetsLeave() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCard));
        prepareHelix();
        harness.castSorcery(player1, 0, graveyardCard.getId(), List.of(permanent.getId()));
        harness.getGameData().playerBattlefields.get(player2.getId()).remove(permanent);
        harness.setGraveyard(player2, List.of(permanent.getCard()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(graveyardCard));

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Aether Helix");
    }

    private void prepareHelix() {
        harness.setHand(player1, List.of(new AetherHelix()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
