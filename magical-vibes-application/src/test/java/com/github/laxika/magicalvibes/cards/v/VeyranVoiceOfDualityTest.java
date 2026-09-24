package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PyrostaticPillar;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeyranVoiceOfDuality.class, BarkshellBlessing.class, ElvishVisionary.class,
        GrizzlyBears.class, PyrostaticPillar.class, Shock.class})
class VeyranVoiceOfDualityTest extends BaseCardTest {

    @Test
    @DisplayName("Veyran doubles its magecraft trigger when an instant is cast")
    void doublesMagecraftOnCast() {
        Permanent veyran = addReadyCreature(player1, new VeyranVoiceOfDuality());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, veyran)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, veyran)).isEqualTo(4);
    }

    @Test
    @DisplayName("Veyran doubles its magecraft trigger when an instant is copied")
    void doublesMagecraftOnCopy() {
        Permanent veyran = addReadyCreature(player1, new VeyranVoiceOfDuality());
        Permanent target = addReadyCreature(player1, new GrizzlyBears());
        Permanent conspireA = addReadyCreature(player1, new GrizzlyBears());
        Permanent conspireB = addReadyCreature(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(),
                List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, veyran)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, veyran)).isEqualTo(6);
    }

    @Test
    @DisplayName("Veyran does not double triggers caused by a creature spell")
    void doesNotDoubleCreatureSpellTrigger() {
        harness.addToBattlefield(player1, new VeyranVoiceOfDuality());
        harness.setHand(player1, List.of(new ElvishVisionary()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Veyran does not double a trigger caused by an opponent's instant")
    void doesNotDoubleOpponentInstantTrigger() {
        harness.addToBattlefield(player1, new VeyranVoiceOfDuality());
        harness.addToBattlefield(player1, new PyrostaticPillar());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
