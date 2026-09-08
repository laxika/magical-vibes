package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PigmentWranglerStrikingPaletteTest extends BaseCardTest {

    @Test
    @DisplayName("Pigment Wrangler enters prepared with a Striking Palette copy in exile")
    void entersPrepared() {
        Permanent wrangler = castPigmentWrangler();

        assertThat(wrangler.isPrepared()).isTrue();
        assertThat(wrangler.getPreparedSpellCardId()).isNotNull();
        assertThat(gd.findExiledCard(wrangler.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    @DisplayName("Casting Striking Palette copies the next instant or sorcery this turn")
    void strikingPaletteCopiesNextInstantOrSorcery() {
        Permanent wrangler = castPigmentWrangler();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, wrangler.getPreparedSpellCardId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(wrangler.isPrepared()).isFalse();
        assertThat(gameData.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gameData.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Copy Lightning Bolt"));
        assertThat(gameData.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
    }

    private Permanent castPigmentWrangler() {
        harness.setHand(player1, List.of(new PigmentWranglerStrikingPalette()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Pigment Wrangler");
    }
}
