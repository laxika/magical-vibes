package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DampenThought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KamiOfTheHunt;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrbweaverKumoTest extends BaseCardTest {

    private Permanent addKumo() {
        harness.addToBattlefield(player1, new OrbweaverKumo());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    @Test
    @DisplayName("Gains forestwalk when you cast a Spirit spell")
    void gainsForestwalkOnSpiritCast() {
        Permanent kumo = addKumo();

        assertThat(gqs.hasKeyword(gd, kumo, Keyword.FORESTWALK)).isFalse();

        harness.setHand(player1, List.of(new KamiOfTheHunt()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kumo, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Gains forestwalk when you cast an Arcane spell")
    void gainsForestwalkOnArcaneCast() {
        Permanent kumo = addKumo();

        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kumo, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger on a spell that is neither Spirit nor Arcane")
    void noTriggerOnUnrelatedSpell() {
        Permanent kumo = addKumo();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kumo, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Forestwalk wears off at end of turn")
    void forestwalkWearsOff() {
        Permanent kumo = addKumo();

        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kumo, Keyword.FORESTWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent afterCleanup = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, afterCleanup, Keyword.FORESTWALK)).isFalse();
    }
}
