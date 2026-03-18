package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrchardSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Orchard Spirit has correct static effect")
    void hasCorrectEffect() {
        OrchardSpirit card = new OrchardSpirit();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(CanBeBlockedOnlyByFilterEffect.class);
        CanBeBlockedOnlyByFilterEffect effect = (CanBeBlockedOnlyByFilterEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.blockerPredicate()).isInstanceOf(PermanentAnyOfPredicate.class);
        assertThat(effect.allowedBlockersDescription()).isEqualTo("creatures with flying or reach");
    }

    @Test
    @DisplayName("Orchard Spirit cannot be blocked by a creature without flying or reach")
    void cannotBeBlockedByNormalCreature() {
        Permanent spirit = attackingSpirit();
        gd.playerBattlefields.get(player1.getId()).add(spirit);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with flying or reach");
    }

    @Test
    @DisplayName("Orchard Spirit can be blocked by a creature with flying")
    void canBeBlockedByFlyingCreature() {
        Permanent spirit = attackingSpirit();
        gd.playerBattlefields.get(player1.getId()).add(spirit);

        Permanent flyer = new Permanent(new AvenFisher());
        flyer.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(flyer);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(flyer.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Orchard Spirit can be blocked by a creature with reach")
    void canBeBlockedByReachCreature() {
        Permanent spirit = attackingSpirit();
        gd.playerBattlefields.get(player1.getId()).add(spirit);

        Permanent spider = new Permanent(new GiantSpider());
        spider.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(spider);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    private Permanent attackingSpirit() {
        Permanent spirit = new Permanent(new OrchardSpirit());
        spirit.setSummoningSick(false);
        spirit.setAttacking(true);
        return spirit;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
