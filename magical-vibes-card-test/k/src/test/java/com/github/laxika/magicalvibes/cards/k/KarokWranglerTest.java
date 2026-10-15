package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineInvocation;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KarokWrangler.class, BarkshellBlessing.class, GiantGrowth.class, GrizzlyBears.class,
        LeylineInvocation.class})
class KarokWranglerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant puts a +1/+1 counter on a chosen creature you control")
    void castingInstantPutsCounterOnChosenCreature() {
        addCreatureReady(player1, new KarokWrangler());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Copying an instant puts a +1/+1 counter on a chosen creature for each trigger")
    void copyingInstantPutsCountersOnChosenCreature() {
        addCreatureReady(player1, new KarokWrangler());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a sorcery can put a counter on Karok Wrangler itself")
    void castingSorceryCanTargetItself() {
        Permanent wrangler = addCreatureReady(player1, new KarokWrangler());
        harness.setHand(player1, List.of(new LeylineInvocation()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0);
        harness.handlePermanentChosen(player1, wrangler.getId());
        harness.passBothPriorities();

        assertThat(wrangler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Casting a creature does not trigger magecraft")
    void castingCreatureDoesNotTriggerMagecraft() {
        Permanent wrangler = addCreatureReady(player1, new KarokWrangler());
        harness.setHand(player1, List.of(new KarokWrangler()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(wrangler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's instant does not trigger magecraft")
    void opponentCastingInstantDoesNotTriggerMagecraft() {
        Permanent wrangler = addCreatureReady(player1, new KarokWrangler());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, wrangler.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(wrangler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Magecraft targets only your creatures independently of the spell's target")
    void magecraftCannotTargetOpponentsCreature() {
        Permanent wrangler = addCreatureReady(player1, new KarokWrangler());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, opponentCreature.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(wrangler.getId());
        harness.handlePermanentChosen(player1, wrangler.getId());
        resolveAllTriggers();

        assertThat(wrangler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Magecraft does not put a counter on a target that changes controllers")
    void targetMustStillBeControlledAtResolution() {
        Permanent wrangler = addCreatureReady(player1, new KarokWrangler());
        harness.setHand(player1, List.of(new LeylineInvocation()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0);
        harness.handlePermanentChosen(player1, wrangler.getId());
        gd.playerBattlefields.get(player1.getId()).remove(wrangler);
        gd.playerBattlefields.get(player2.getId()).add(wrangler);
        resolveAllTriggers();

        assertThat(wrangler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
