package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IridescentAngel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Dematerialize.class, Forest.class, AvenFisher.class, IridescentAngel.class})
class DematerializeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target permanent to its owner's hand")
    void returnsTargetPermanentToOwnersHand() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        harness.setHand(player1, List.of(new Dematerialize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInHand(player2, "Forest");
        harness.assertInGraveyard(player1, "Dematerialize");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Dematerialize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent with protection from blue")
    void cannotTargetPermanentWithProtectionFromBlue() {
        harness.addToBattlefield(player2, new IridescentAngel());
        UUID targetId = harness.getPermanentId(player2, "Iridescent Angel");

        harness.setHand(player1, List.of(new Dematerialize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Flashback returns target permanent and exiles the spell")
    void flashbackReturnsTargetPermanentAndExilesSpell() {
        harness.addToBattlefield(player2, new AvenFisher());
        UUID targetId = harness.getPermanentId(player2, "Aven Fisher");

        harness.setGraveyard(player1, List.of(new Dematerialize()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castAndResolveFlashback(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Aven Fisher");
        harness.assertInHand(player2, "Aven Fisher");
        harness.assertNotInGraveyard(player1, "Dematerialize");

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Dematerialize"));
    }

    @Test
    @DisplayName("Can return a permanent controlled by the caster")
    void returnsOwnPermanent() {
        harness.addToBattlefield(player1, new Forest());
        UUID targetId = harness.getPermanentId(player1, "Forest");
        harness.setHand(player1, List.of(new Dematerialize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Dematerialize");
    }

    @Test
    @DisplayName("Returns a permanent to its owner rather than its controller")
    void returnsPermanentToOwnerRatherThanController() {
        Forest forest = new Forest();
        forest.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, forest);
        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new Dematerialize()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInHand(player1, "Forest");
        harness.assertNotInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Flashback requires two blue mana")
    void flashbackRequiresTwoBlueMana() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.setGraveyard(player1, List.of(new Dematerialize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Not enough mana");

        harness.assertInGraveyard(player1, "Dematerialize");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Flashback still exiles the spell when its target leaves before resolution")
    void flashbackExilesSpellWhenTargetLeaves() {
        var target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setGraveyard(player1, List.of(new Dematerialize()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castFlashback(player1, 0, target.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(harness.getGameData(), target));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotInHand(player2, "Forest");
        harness.assertNotInGraveyard(player1, "Dematerialize");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Dematerialize"));
        assertThat(harness.getGameData().stack).isEmpty();
    }

}
