package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IncreasingDevotionTest extends BaseCardTest {

    private List<Permanent> humanTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Human"))
                .toList();
    }

    

    @Test
    @DisplayName("Normal cast creates five 1/1 white Human tokens")
    void normalCastCreatesFiveHumans() {
        harness.setHand(player1, List.of(new IncreasingDevotion()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = humanTokens();
        assertThat(tokens).hasSize(5);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.HUMAN);
        }
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Increasing Devotion"));
    }

    @Test
    @DisplayName("Flashback creates ten Human tokens and exiles the spell")
    void flashbackCreatesTenHumansAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new IncreasingDevotion()));
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(humanTokens()).hasSize(10);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Increasing Devotion"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Increasing Devotion"));
    }

    @Test
    @DisplayName("Flashback puts the sorcery on stack as cast with flashback")
    void flashbackPutsSpellOnStack() {
        harness.setGraveyard(player1, List.of(new IncreasingDevotion()));
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Increasing Devotion");
        assertThat(entry.isCastWithFlashback()).isTrue();
        assertThat(entry.getSourceZone()).isEqualTo(Zone.GRAVEYARD);
    }
}
