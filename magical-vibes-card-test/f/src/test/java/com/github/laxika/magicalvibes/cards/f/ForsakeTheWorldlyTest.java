package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AnointedProcession;
import com.github.laxika.magicalvibes.cards.t.ThoseWhoServe;
import com.github.laxika.magicalvibes.cards.l.LuxaRiverShrine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForsakeTheWorldly.class, AnointedProcession.class, ThoseWhoServe.class, LuxaRiverShrine.class})
class ForsakeTheWorldlyTest extends BaseCardTest {

    private void castForsake(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ForsakeTheWorldly()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetId);
    }

    @Test
    @DisplayName("Exiles target artifact")
    void exilesArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LuxaRiverShrine()).getId();
        castForsake(targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Luxa River Shrine");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Luxa River Shrine"));
    }

    @Test
    @DisplayName("Exiles target enchantment")
    void exilesEnchantment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AnointedProcession()).getId();
        castForsake(targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Anointed Procession");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Anointed Procession"));
    }

    @Test
    @DisplayName("Cannot target a nonartifact, nonenchantment creature")
    void cannotTargetCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new ThoseWhoServe()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ForsakeTheWorldly()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Forsake the Worldly and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ForsakeTheWorldly()));
        harness.setLibrary(player1, List.of(new ThoseWhoServe()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Forsake the Worldly");
        harness.assertNotInHand(player1, "Forsake the Worldly");
        harness.assertNotInHand(player1, "Those Who Serve");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Forsake the Worldly");
        harness.assertInHand(player1, "Those Who Serve");
    }

    @Test
    @DisplayName("Cycling requires two mana and leaves the card in hand when payment fails")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new ForsakeTheWorldly()));
        harness.setLibrary(player1, List.of(new ThoseWhoServe()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Forsake the Worldly");
        harness.assertNotInGraveyard(player1, "Forsake the Worldly");
        harness.assertNotInHand(player1, "Those Who Serve");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can exile an artifact controlled by the caster")
    void exilesOwnArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new LuxaRiverShrine()).getId();
        castForsake(targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Luxa River Shrine");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Luxa River Shrine"));
        harness.assertInGraveyard(player1, "Forsake the Worldly");
    }

    @Test
    @DisplayName("Does not exile another permanent when the target leaves before resolution")
    void targetLeavesBeforeResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LuxaRiverShrine()).getId();
        harness.addToBattlefield(player2, new AnointedProcession());
        castForsake(targetId);
        harness.setHand(player2, List.of(new ForsakeTheWorldly()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Anointed Procession");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .hasSize(1)
                .anyMatch(c -> c.getName().equals("Luxa River Shrine"));
        harness.assertInGraveyard(player1, "Forsake the Worldly");
        harness.assertInGraveyard(player2, "Forsake the Worldly");
        assertThat(gd.stack).isEmpty();
    }
}
