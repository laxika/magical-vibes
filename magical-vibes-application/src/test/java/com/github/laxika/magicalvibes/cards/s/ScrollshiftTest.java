package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkHeartOfTheWood;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KorHalberd;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scrollshift.class, DarkHeartOfTheWood.class, GrizzlyBears.class, KorHalberd.class})
class ScrollshiftTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers an artifact, creature, or enchantment and draws a card")
    void flickersEachSupportedPermanentTypeAndDrawsCards() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new KorHalberd());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new DarkHeartOfTheWood());
        harness.setHand(player1, List.of(new Scrollshift(), new Scrollshift(), new Scrollshift()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castAndResolveInstant(player1, 0, artifact.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, enchantment.getId());

        assertThat(harness.getPermanentId(player1, "Kor Halberd")).isNotEqualTo(artifact.getId());
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(creature.getId());
        assertThat(harness.getPermanentId(player1, "Dark Heart of the Wood"))
                .isNotEqualTo(enchantment.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Allows choosing no permanent and still draws a card")
    void allowsNoTargetAndDrawsCard() {
        harness.setHand(player1, List.of(new Scrollshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent an opponent controls")
    void cannotTargetOpponentPermanent() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Scrollshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, creature, or enchantment you control");
    }

    @Test
    @CardUsed({Scrollshift.class, KorHalberd.class})
    @DisplayName("A borrowed artifact returns untapped to its owner and the caster draws")
    void borrowedArtifactReturnsToOwner() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new KorHalberd());
        gd.stolenCreatures.put(original.getId(), player2.getId());
        original.tap();
        harness.setHand(player1, List.of(new Scrollshift()));
        harness.setLibrary(player1, List.of(new KorHalberd()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, original.getId());

        harness.assertNotOnBattlefield(player1, "Kor Halberd");
        Permanent returned = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Kor Halberd"))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.isTapped()).isFalse();
        harness.assertInHand(player1, "Kor Halberd");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @CardUsed({Scrollshift.class, KorHalberd.class})
    @DisplayName("Does not draw when its chosen target leaves before resolution")
    void absentTargetPreventsDraw() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new KorHalberd());
        harness.setHand(player1, List.of(new Scrollshift()));
        harness.setLibrary(player1, List.of(new KorHalberd()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, original.getId());
        gd.playerBattlefields.get(player1.getId()).remove(original);
        gd.playerGraveyards.get(player1.getId()).add(original.getCard());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kor Halberd");
        harness.assertInGraveyard(player1, "Kor Halberd");
        harness.assertInGraveyard(player1, "Scrollshift");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({Scrollshift.class, KorHalberd.class})
    @DisplayName("Does not flicker or draw if the target changes controller before resolution")
    void controlChangeMakesTargetIllegal() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new KorHalberd());
        harness.setHand(player1, List.of(new Scrollshift()));
        harness.setLibrary(player1, List.of(new KorHalberd()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, original.getId());
        gd.playerBattlefields.get(player1.getId()).remove(original);
        gd.playerBattlefields.get(player2.getId()).add(original);
        gd.stolenCreatures.put(original.getId(), player1.getId());

        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player2, "Kor Halberd")).isEqualTo(original.getId());
        harness.assertNotOnBattlefield(player1, "Kor Halberd");
        harness.assertInGraveyard(player1, "Scrollshift");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
