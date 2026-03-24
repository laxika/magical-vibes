package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KumenasSpeaker;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiverHeraldsBoonTest extends BaseCardTest {

    @Test
    @DisplayName("Card has two PutPlusOnePlusOneCounterOnTargetCreatureEffect effects")
    void cardHasCorrectEffects() {
        RiverHeraldsBoon card = new RiverHeraldsBoon();
        long counterEffects = card.getEffects(EffectSlot.SPELL).stream()
                .filter(e -> e instanceof PutPlusOnePlusOneCounterOnTargetCreatureEffect)
                .count();
        assertThat(counterEffects).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts +1/+1 counter on target creature and target Merfolk")
    void putsCounterOnCreatureAndMerfolk() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new KumenasSpeaker());
        harness.setHand(player1, List.of(new RiverHeraldsBoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID merfolkId = harness.getPermanentId(player1, "Kumena's Speaker");
        harness.castInstant(player1, 0, List.of(bearId, merfolkId));
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears")).findFirst().orElseThrow();
        Permanent merfolk = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kumena's Speaker")).findFirst().orElseThrow();
        assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(merfolk.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts +1/+1 counter on creature only when no Merfolk target chosen")
    void putsCounterOnCreatureOnlyWithoutMerfolkTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RiverHeraldsBoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(bearId));
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears")).findFirst().orElseThrow();
        assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Merfolk targeted as both creature and Merfolk gets two +1/+1 counters (CR 114.6c)")
    void merfolkTargetedAsBothGetsTwoCounters() {
        harness.addToBattlefield(player1, new KumenasSpeaker());
        harness.setHand(player1, List.of(new RiverHeraldsBoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID merfolkId = harness.getPermanentId(player1, "Kumena's Speaker");
        harness.castInstant(player1, 0, List.of(merfolkId, merfolkId));
        harness.passBothPriorities();

        Permanent merfolk = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kumena's Speaker")).findFirst().orElseThrow();
        assertThat(merfolk.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-Merfolk creature as the Merfolk target")
    void cannotTargetNonMerfolkAsSecondTarget() {
        GrizzlyBears bear1 = new GrizzlyBears();
        GrizzlyBears bear2 = new GrizzlyBears();
        harness.addToBattlefield(player1, bear1);
        harness.addToBattlefield(player1, bear2);
        harness.setHand(player1, List.of(new RiverHeraldsBoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(id1, id2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Merfolk");
    }

    @Test
    @DisplayName("Can target opponent's creatures")
    void canTargetOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new KumenasSpeaker());
        harness.setHand(player1, List.of(new RiverHeraldsBoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID merfolkId = harness.getPermanentId(player2, "Kumena's Speaker");
        harness.castInstant(player1, 0, List.of(bearId, merfolkId));
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears")).findFirst().orElseThrow();
        Permanent merfolk = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kumena's Speaker")).findFirst().orElseThrow();
        assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(merfolk.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Spell resolves on remaining target when second target is removed")
    void resolvesOnRemainingTargetWhenSecondTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new KumenasSpeaker());
        harness.setHand(player1, List.of(new RiverHeraldsBoon()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID merfolkId = harness.getPermanentId(player1, "Kumena's Speaker");
        harness.castInstant(player1, 0, List.of(bearId, merfolkId));

        // Remove Merfolk before resolution
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(merfolkId));

        harness.passBothPriorities();

        // First target should still get its counter
        Permanent bear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears")).findFirst().orElseThrow();
        assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(1);
    }
}
