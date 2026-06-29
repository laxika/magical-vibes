package com.github.laxika.magicalvibes.cards.b;

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
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCardsInControllerGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoneyardWurmTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Boneyard Wurm has static P/T effect counting creature cards in controller's graveyard")
    void hasCorrectStaticEffect() {
        BoneyardWurm card = new BoneyardWurm();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(PowerToughnessEqualToCardsInControllerGraveyardEffect.class);
        var effect = (PowerToughnessEqualToCardsInControllerGraveyardEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.filter()).isEqualTo(new CardTypePredicate(CardType.CREATURE));
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Boneyard Wurm puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new BoneyardWurm()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Boneyard Wurm");
    }

    @Test
    @DisplayName("Resolving Boneyard Wurm puts it on the battlefield when graveyard has creatures")
    void resolvingPutsItOnBattlefield() {
        harness.setGraveyard(player1, createCreatureCards(2));
        harness.setHand(player1, List.of(new BoneyardWurm()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Boneyard Wurm"));
    }

    @Test
    @DisplayName("Boneyard Wurm dies to state-based actions when no creatures in controller's graveyard")
    void diesWhenNoCreaturesInGraveyard() {
        harness.setHand(player1, List.of(new BoneyardWurm()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // 0/0 creature dies to SBA — but then Boneyard Wurm itself is a creature card in the graveyard,
        // making it effectively a 1/1 while in play next time. However, SBA checks P/T on the battlefield
        // where it's 0/0 before going to the graveyard.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Boneyard Wurm"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Boneyard Wurm"));
    }

    // ===== Dynamic power/toughness =====

    @Test
    @DisplayName("Boneyard Wurm is 0/0 with no creature cards in controller's graveyard")
    void isZeroZeroWithEmptyGraveyard() {
        Permanent perm = addBoneyardWurmReady(player1);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Boneyard Wurm P/T equals number of creature cards in controller's graveyard")
    void ptEqualsCreatureCountInOwnGraveyard() {
        Permanent perm = addBoneyardWurmReady(player1);
        harness.setGraveyard(player1, createCreatureCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boneyard Wurm does NOT count creature cards in opponent's graveyard")
    void doesNotCountOpponentsGraveyard() {
        Permanent perm = addBoneyardWurmReady(player1);
        harness.setGraveyard(player2, createCreatureCards(4));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Boneyard Wurm only counts creature cards, not non-creature cards")
    void onlyCountsCreatureCards() {
        Permanent perm = addBoneyardWurmReady(player1);

        List<Card> graveyard = new ArrayList<>();
        graveyard.addAll(createCreatureCards(2));
        graveyard.add(new Plains());
        graveyard.add(new DarksteelAxe());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boneyard Wurm P/T updates when creatures are added to graveyard")
    void ptUpdatesWhenCreaturesAddedToGraveyard() {
        Permanent perm = addBoneyardWurmReady(player1);
        harness.setGraveyard(player1, createCreatureCards(1));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boneyard Wurm counts only controller's graveyard when both have creatures")
    void countsOnlyControllerGraveyardWhenBothHaveCreatures() {
        Permanent perm = addBoneyardWurmReady(player1);
        harness.setGraveyard(player1, createCreatureCards(2));
        harness.setGraveyard(player2, createCreatureCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    // ===== Helper methods =====

    private Permanent addBoneyardWurmReady(Player player) {
        BoneyardWurm card = new BoneyardWurm();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private List<Card> createCreatureCards(int count) {
        List<Card> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(new GrizzlyBears());
        }
        return creatures;
    }
}
