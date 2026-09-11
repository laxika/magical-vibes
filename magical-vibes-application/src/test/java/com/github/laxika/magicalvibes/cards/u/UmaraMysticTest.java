package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UmaraMystic.class, Shock.class, Divination.class, FugitiveWizard.class, GrizzlyBears.class})
class UmaraMysticTest extends BaseCardTest {

    @Test
    void instantAndSorcerySpellsBoostSelf() {
        Permanent mystic = addMystic(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new Shock(), new Divination()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(mystic.getPowerModifier()).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(mystic.getPowerModifier()).isEqualTo(4);
    }

    @Test
    void wizardSpellBoostsSelf() {
        Permanent mystic = addMystic(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new FugitiveWizard()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(mystic.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void nonMatchingCreatureSpellDoesNotTrigger() {
        Permanent mystic = addMystic(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(mystic.getPowerModifier()).isEqualTo(0);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent mystic = addMystic(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(mystic.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mystic.getPowerModifier()).isEqualTo(0);
    }

    private Permanent addMystic(Player player) {
        Permanent mystic = new Permanent(new UmaraMystic());
        mystic.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(mystic);
        return mystic;
    }
}
