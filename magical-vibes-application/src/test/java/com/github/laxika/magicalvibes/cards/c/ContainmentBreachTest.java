package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContainmentBreachTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a low-mana-value artifact and creates a Pest")
    void destroysLowManaValueArtifactAndCreatesPest() {
        harness.addToBattlefield(player2, new Millstone());
        UUID targetId = harness.getPermanentId(player2, "Millstone");
        castContainmentBreach(targetId);

        assertNotOnBattlefield(targetId);
        assertThat(createdTokens()).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Pest for an artifact with mana value greater than 2")
    void doesNotCreatePestForHighManaValueArtifact() {
        harness.addToBattlefield(player2, new RodOfRuin());
        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        castContainmentBreach(targetId);

        assertNotOnBattlefield(targetId);
        assertThat(createdTokens()).isEmpty();
    }

    @Test
    @DisplayName("Creates a Pest even when a low-mana-value target is indestructible")
    void createsPestWhenTargetIsIndestructible() {
        harness.addToBattlefield(player2, new DarksteelCitadel());
        UUID targetId = harness.getPermanentId(player2, "Darksteel Citadel");
        castContainmentBreach(targetId);

        assertThat(gd.playerBattlefields.get(player2.getId()).stream().map(Permanent::getId))
                .contains(targetId);
        assertThat(createdTokens()).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a nonartifact, nonenchantment permanent")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareContainmentBreach();

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    private void castContainmentBreach(UUID targetId) {
        prepareContainmentBreach();
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void prepareContainmentBreach() {
        harness.setHand(player1, List.of(new ContainmentBreach()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void assertNotOnBattlefield(UUID targetId) {
        assertThat(gd.playerBattlefields.get(player2.getId()).stream().map(Permanent::getId))
                .doesNotContain(targetId);
    }

    private List<Permanent> createdTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
