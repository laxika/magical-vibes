package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WelkinTern;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShootDownTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target artifact")
    void exilesArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        cast(target.getId());

        assertExiled(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Exiles a target enchantment")
    void exilesEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());
        cast(target.getId());

        assertExiled(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Exiles a target creature with flying")
    void exilesFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WelkinTern());
        cast(target.getId());

        assertExiled(player2, "Welkin Tern");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void rejectsNonFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, enchantment, or creature with flying");
    }

    @Test
    @DisplayName("Cannot target a land")
    void rejectsLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, enchantment, or creature with flying");
    }

    private void cast(UUID targetId) {
        prepareCast();
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new ShootDown()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void assertExiled(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        harness.assertNotOnBattlefield(player, cardName);
        harness.assertNotInGraveyard(player, cardName);
        assertThat(gd.getPlayerExiledCards(player.getId()))
                .anyMatch(card -> card.getName().equals(cardName));
    }
}
