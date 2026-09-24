package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrrikSonOfYawgmoth.class, FeralShadow.class})
class KrrikSonOfYawgmothTest extends BaseCardTest {

    @Test
    @DisplayName("The controller may pay 2 life for each black mana in a spell's cost")
    void paysLifeForBlackMana() {
        addKrrik();
        harness.setHand(player1, List.of(new FeralShadow()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Casting a black spell puts a +1/+1 counter on K'rrik")
    void blackSpellAddsCounter() {
        Permanent krrik = addKrrik();
        harness.setHand(player1, List.of(new FeralShadow()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, krrik)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, krrik)).isEqualTo(3);
    }

    private Permanent addKrrik() {
        Permanent krrik = addCreatureReady(player1, new KrrikSonOfYawgmoth());
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return krrik;
    }
}
