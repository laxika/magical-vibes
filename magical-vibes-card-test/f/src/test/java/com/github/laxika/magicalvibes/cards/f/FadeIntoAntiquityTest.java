package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.n.NyleaGodOfTheHunt;
import com.github.laxika.magicalvibes.cards.s.SatyrRambler;
import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.v.VoyagesEnd;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FadeIntoAntiquity.class, BronzeSable.class, NyleaGodOfTheHunt.class,
        SatyrRambler.class, VoyagesEnd.class})
class FadeIntoAntiquityTest extends BaseCardTest {

    private void castFadeIntoAntiquity(UUID targetId) {
        harness.setHand(player1, List.of(new FadeIntoAntiquity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    @Test
    @DisplayName("Exiles target artifact")
    void exilesArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BronzeSable()).getId();

        castFadeIntoAntiquity(targetId);

        harness.assertNotOnBattlefield(player2, "Bronze Sable");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Bronze Sable"));
    }

    @Test
    @DisplayName("Exiles target enchantment")
    void exilesEnchantment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new NyleaGodOfTheHunt()).getId();

        castFadeIntoAntiquity(targetId);

        harness.assertNotOnBattlefield(player2, "Nylea, God of the Hunt");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Nylea, God of the Hunt"));
    }

    @Test
    @DisplayName("Cannot target a nonartifact, nonenchantment creature")
    void cannotTargetCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new SatyrRambler()).getId();
        harness.setHand(player1, List.of(new FadeIntoAntiquity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can exile your own artifact without affecting another artifact")
    void exilesOwnArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new BronzeSable()).getId();
        harness.addToBattlefield(player2, new BronzeSable());

        castFadeIntoAntiquity(targetId);

        harness.assertNotOnBattlefield(player1, "Bronze Sable");
        harness.assertOnBattlefield(player2, "Bronze Sable");
        harness.assertNotInGraveyard(player1, "Bronze Sable");
        harness.assertInGraveyard(player1, "Fade into Antiquity");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Bronze Sable"));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not exile a target returned to hand in response")
    void targetLeavesBattlefieldBeforeResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BronzeSable()).getId();
        harness.setHand(player1, List.of(new FadeIntoAntiquity()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, targetId);

        harness.setHand(player2, List.of(new VoyagesEnd()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Bronze Sable");
        harness.assertNotOnBattlefield(player2, "Bronze Sable");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Fade into Antiquity");
    }
}
