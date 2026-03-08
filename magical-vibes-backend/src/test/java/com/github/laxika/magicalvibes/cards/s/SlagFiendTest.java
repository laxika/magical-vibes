package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SlagFiendTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Slag Fiend has static P/T effect counting artifact cards in all graveyards")
    void hasCorrectStaticEffect() {
        SlagFiend card = new SlagFiend();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(PowerToughnessEqualToCardsInAllGraveyardsEffect.class);
        var effect = (PowerToughnessEqualToCardsInAllGraveyardsEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.filter()).isEqualTo(new CardTypePredicate(CardType.ARTIFACT));
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Slag Fiend puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new SlagFiend()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Slag Fiend");
    }

    @Test
    @DisplayName("Resolving Slag Fiend puts it on the battlefield when graveyard has artifacts")
    void resolvingPutsItOnBattlefield() {
        harness.setGraveyard(player1, createArtifactCards(2));
        harness.setHand(player1, List.of(new SlagFiend()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Slag Fiend"));
    }

    @Test
    @DisplayName("Slag Fiend dies to state-based actions when no artifacts in any graveyard")
    void diesWhenNoArtifactsInGraveyards() {
        harness.setHand(player1, List.of(new SlagFiend()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // 0/0 creature dies to SBA
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Slag Fiend"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Slag Fiend"));
    }

    // ===== Dynamic power/toughness =====

    @Test
    @DisplayName("Slag Fiend is 0/0 with no artifact cards in any graveyard")
    void isZeroZeroWithEmptyGraveyards() {
        Permanent perm = addSlagFiendReady(player1);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Slag Fiend P/T equals number of artifact cards in controller's graveyard")
    void ptEqualsArtifactCountInOwnGraveyard() {
        Permanent perm = addSlagFiendReady(player1);
        harness.setGraveyard(player1, createArtifactCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Slag Fiend P/T counts artifact cards in ALL graveyards")
    void ptCountsAllGraveyards() {
        Permanent perm = addSlagFiendReady(player1);
        harness.setGraveyard(player1, createArtifactCards(2));
        harness.setGraveyard(player2, createArtifactCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Slag Fiend only counts artifact cards, not non-artifact cards")
    void onlyCountsArtifactCards() {
        Permanent perm = addSlagFiendReady(player1);

        List<Card> graveyard = new ArrayList<>();
        graveyard.addAll(createArtifactCards(2));
        graveyard.add(new Plains());
        graveyard.add(new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Slag Fiend P/T updates when artifacts are added to graveyard")
    void ptUpdatesWhenArtifactsAddedToGraveyard() {
        Permanent perm = addSlagFiendReady(player1);
        harness.setGraveyard(player1, createArtifactCards(1));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new DarksteelAxe());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Slag Fiend P/T counts artifact creatures (they are artifacts)")
    void ptCountsArtifactCreatures() {
        Permanent perm = addSlagFiendReady(player1);

        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new Sickleslicer()); // Artifact creature (equipment with living weapon)
        graveyard.add(new DarksteelAxe()); // Pure artifact
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Slag Fiend P/T counts opponent's graveyard artifacts too")
    void ptCountsOpponentsGraveyard() {
        Permanent perm = addSlagFiendReady(player1);
        harness.setGraveyard(player2, createArtifactCards(4));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(4);
    }

    // ===== Helper methods =====

    private Permanent addSlagFiendReady(Player player) {
        SlagFiend card = new SlagFiend();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private List<Card> createArtifactCards(int count) {
        List<Card> artifacts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            artifacts.add(new DarksteelAxe());
        }
        return artifacts;
    }
}
