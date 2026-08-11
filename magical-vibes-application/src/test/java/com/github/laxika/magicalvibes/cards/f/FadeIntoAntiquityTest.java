package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FadeIntoAntiquityTest extends BaseCardTest {

    private void castFadeIntoAntiquity(UUID targetId) {
        harness.setHand(player1, List.of(new FadeIntoAntiquity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Exiles target artifact")
    void exilesArtifact() {
        harness.addToBattlefield(player2, new Ornithopter());
        UUID targetId = harness.getPermanentId(player2, "Ornithopter");

        castFadeIntoAntiquity(targetId);

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Ornithopter"));
    }

    @Test
    @DisplayName("Exiles target enchantment")
    void exilesEnchantment() {
        harness.addToBattlefield(player2, new AuraOfSilence());
        UUID targetId = harness.getPermanentId(player2, "Aura of Silence");

        castFadeIntoAntiquity(targetId);

        harness.assertNotOnBattlefield(player2, "Aura of Silence");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Aura of Silence"));
    }

    @Test
    @DisplayName("Cannot target a nonartifact, nonenchantment creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new FadeIntoAntiquity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
