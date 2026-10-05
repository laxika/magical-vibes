package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.m.MysticReflection;
import com.github.laxika.magicalvibes.cards.s.SilverclawGriffin;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IncreasingDevotion.class, MysticReflection.class, SilverclawGriffin.class})
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

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> tokens = humanTokens();
        assertThat(tokens).hasSize(5);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.HUMAN);
        }
        harness.assertInGraveyard(player1, "Increasing Devotion");
    }

    @Test
    @DisplayName("Flashback creates ten Human tokens and exiles the spell")
    void flashbackCreatesTenHumansAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new IncreasingDevotion()));
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(humanTokens()).hasSize(10);
        harness.assertNotInGraveyard(player1, "Increasing Devotion");
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

    @Test
    @DisplayName("All ten graveyard-cast tokens enter together under Mystic Reflection")
    void graveyardTokensEnterAsOneGroup() {
        harness.addToBattlefield(player1, new SilverclawGriffin());
        harness.setHand(player1, List.of(new MysticReflection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Silverclaw Griffin"));

        harness.setGraveyard(player1, List.of(new IncreasingDevotion()));
        harness.addMana(player1, ManaColor.WHITE, 9);
        harness.castAndResolveFlashback(player1, 0, null);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(10);
        assertThat(tokens).allSatisfy(token ->
                assertThat(token.getCard().getName()).isEqualTo("Silverclaw Griffin"));
        harness.assertNotInGraveyard(player1, "Increasing Devotion");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Increasing Devotion"));
    }
}
