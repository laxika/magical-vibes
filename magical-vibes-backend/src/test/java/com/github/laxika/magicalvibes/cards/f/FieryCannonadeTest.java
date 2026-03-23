package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DireFleetCaptain;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FieryCannonadeTest extends BaseCardTest {

    @Test
    @DisplayName("Fiery Cannonade has correct effect configuration")
    void hasCorrectEffect() {
        FieryCannonade card = new FieryCannonade();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(MassDamageEffect.class);
        MassDamageEffect effect = (MassDamageEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.damage()).isEqualTo(2);
        assertThat(effect.damagesPlayers()).isFalse();
        assertThat(effect.filter()).isInstanceOf(PermanentNotPredicate.class);
        PermanentNotPredicate notPred = (PermanentNotPredicate) effect.filter();
        assertThat(notPred.predicate()).isInstanceOf(PermanentHasSubtypePredicate.class);
        PermanentHasSubtypePredicate subtypePred = (PermanentHasSubtypePredicate) notPred.predicate();
        assertThat(subtypePred.subtype()).isEqualTo(CardSubtype.PIRATE);
    }

    @Test
    @DisplayName("Fiery Cannonade kills non-Pirate creatures with toughness 2 or less on both sides")
    void killsNonPirateCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FieryCannonade()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Fiery Cannonade does not damage Pirate creatures")
    void doesNotDamagePirateCreatures() {
        harness.addToBattlefield(player2, new DireFleetCaptain());
        harness.setHand(player1, List.of(new FieryCannonade()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dire Fleet Captain"));
    }

    @Test
    @DisplayName("Fiery Cannonade damages non-Pirate creatures but leaves Pirate creatures unharmed")
    void selectivelyDamages() {
        harness.addToBattlefield(player2, new DireFleetCaptain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FieryCannonade()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dire Fleet Captain"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Fiery Cannonade does not deal damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FieryCannonade()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
