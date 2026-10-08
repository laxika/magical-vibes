package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Guttersnipe;
import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WitherbloomApprentice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeyranVoiceOfDuality.class, WitherbloomApprentice.class,
        BarkshellBlessing.class, Shock.class, GrizzlyBears.class, LavaSpike.class, Guttersnipe.class})
class VeyranVoiceOfDualityTest extends BaseCardTest {

    @Test
    @DisplayName("Veyran doubles its own magecraft trigger")
    void doublesItsOwnMagecraftTrigger() {
        Permanent veyran = addCreatureReady(player1, new VeyranVoiceOfDuality());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(veyran.getEffectivePower()).isEqualTo(4);
        assertThat(veyran.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Veyran doubles another permanent's magecraft trigger")
    void doublesAnotherPermanentMagecraftTrigger() {
        Permanent veyran = addCreatureReady(player1, new VeyranVoiceOfDuality());
        addCreatureReady(player1, new WitherbloomApprentice());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(veyran.getEffectivePower()).isEqualTo(4);
        assertThat(veyran.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Veyran doubles magecraft from a copied instant")
    void doublesMagecraftFromCopiedInstant() {
        Permanent veyran = addCreatureReady(player1, new VeyranVoiceOfDuality());
        addCreatureReady(player1, new WitherbloomApprentice());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(veyran.getEffectivePower()).isEqualTo(6);
        assertThat(veyran.getEffectiveToughness()).isEqualTo(6);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("A sorcery doubles magecraft and other cast triggers")
    void doublesSorceryCastTriggers() {
        Permanent veyran = addCreatureReady(player1, new VeyranVoiceOfDuality());
        addCreatureReady(player1, new Guttersnipe());
        harness.setHand(player1, List.of(new LavaSpike()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(veyran.getEffectivePower()).isEqualTo(4);
        assertThat(veyran.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Casting a creature does not trigger magecraft")
    void creatureSpellDoesNotTriggerMagecraft() {
        Permanent veyran = addCreatureReady(player1, new VeyranVoiceOfDuality());
        addCreatureReady(player1, new WitherbloomApprentice());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(veyran.getEffectivePower()).isEqualTo(2);
        assertThat(veyran.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent's cast does not trigger Veyran or double their magecraft")
    void doesNotAffectOpponentCastTriggers() {
        Permanent veyran = addCreatureReady(player1, new VeyranVoiceOfDuality());
        addCreatureReady(player2, new WitherbloomApprentice());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(veyran.getEffectivePower()).isEqualTo(2);
        assertThat(veyran.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Magecraft boosts expire during cleanup")
    void boostExpiresAtEndOfTurn() {
        Permanent veyran = addCreatureReady(player1, new VeyranVoiceOfDuality());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(veyran.getEffectivePower()).isEqualTo(4);
        assertThat(veyran.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(veyran.getEffectivePower()).isEqualTo(2);
        assertThat(veyran.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Magecraft triggers survive removal of Veyran")
    void otherMagecraftTriggersSurviveVeyranLeaving() {
        Permanent veyran = addCreatureReady(player1, new VeyranVoiceOfDuality());
        addCreatureReady(player1, new WitherbloomApprentice());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, veyran.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(veyran);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
