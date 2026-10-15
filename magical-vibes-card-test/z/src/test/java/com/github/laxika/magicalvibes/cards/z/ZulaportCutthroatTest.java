package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CarrierThrall;
import com.github.laxika.magicalvibes.cards.c.CompleteDisregard;
import com.github.laxika.magicalvibes.cards.r.RisingMiasma;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZulaportCutthroat.class, GrizzlyBears.class, Shock.class,
        CarrierThrall.class, CompleteDisregard.class, RisingMiasma.class})
class ZulaportCutthroatTest extends BaseCardTest {

    @Test
    @DisplayName("An ally creature dying makes each opponent lose 1 life and gains you 1 life")
    void allyCreatureDeathDrainsOpponent() {
        harness.addToBattlefield(player1, new ZulaportCutthroat());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player1, creature);

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Zulaport Cutthroat's own death triggers its ability")
    void selfDeathDrainsOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent cutthroat = harness.addToBattlefieldAndReturn(player1, new ZulaportCutthroat());
        killWithShock(player1, cutthroat);

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger Zulaport Cutthroat")
    void opponentCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new ZulaportCutthroat());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player1, creature);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each Cutthroat sees itself and the other Cutthroat die simultaneously")
    void simultaneousDeathsTriggerEachCutthroatForEachControlledCreature() {
        harness.addToBattlefield(player1, new ZulaportCutthroat());
        harness.addToBattlefield(player1, new ZulaportCutthroat());
        harness.addToBattlefield(player2, new ZulaportCutthroat());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RisingMiasma()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.stack).hasSize(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        resolveAllTriggers();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Sacrificing an Eldrazi Scion token triggers the drain")
    void creatureTokenSacrificeDrainsOpponent() {
        harness.addToBattlefield(player1, new ZulaportCutthroat());
        Permanent thrall = harness.addToBattlefieldAndReturn(player1, new CarrierThrall());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player1, thrall);
        resolveAllTriggers();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        Permanent scion = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);

        harness.activateAbility(player1, scionIndex, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scion);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Exiling a controlled creature does not count as dying")
    void exileDoesNotTriggerDrain() {
        harness.addToBattlefield(player1, new ZulaportCutthroat());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ZulaportCutthroat());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CompleteDisregard()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, other.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(other);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, creature.getId());
        harness.passBothPriorities();
    }
}
