package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SliceInTwainTest extends BaseCardTest {

    @Test
    @DisplayName("Slice in Twain has correct card properties")
    void hasCorrectProperties() {
        SliceInTwain card = new SliceInTwain();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DestroyTargetPermanentEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DrawCardEffect.class);
    }

    @Test
    @DisplayName("Casting Slice in Twain puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SliceInTwain()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Slice in Twain");
        assertThat(entry.getTargetPermanentId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving destroys target artifact and draws a card")
    void destroysArtifactAndDraws() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new SliceInTwain()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fountain of Youth"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fountain of Youth"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Resolving destroys target enchantment and draws a card")
    void destroysEnchantmentAndDraws() {
        harness.addToBattlefield(player2, new AngelicChorus());
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new SliceInTwain()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Slice in Twain goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SliceInTwain()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Slice in Twain"));
    }

    @Test
    @DisplayName("Slice in Twain fizzles and does not draw when target is removed before resolution")
    void fizzlesAndDoesNotDrawWhenTargetRemoved() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new SliceInTwain()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Slice in Twain"));
    }

    @Test
    @DisplayName("Cannot target a creature with Slice in Twain")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SliceInTwain()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
