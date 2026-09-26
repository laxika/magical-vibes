package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BlessedBreath;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HorizonSeed.class, BlessedBreath.class, HarshDeceiver.class,
        IsamaruHoundOfKonda.class, SakuraTribeElder.class})
class HorizonSeedTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell gives a target creature a regeneration shield")
    void arcaneSpellRegeneratesTargetCreature() {
        harness.addToBattlefield(player1, new HorizonSeed());
        Permanent target = addCreatureReady(player1, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a Spirit spell gives a target creature a regeneration shield")
    void spiritSpellRegeneratesTargetCreature() {
        harness.addToBattlefield(player1, new HorizonSeed());
        Permanent target = addCreatureReady(player1, new IsamaruHoundOfKonda());
        harness.castFromHand(player1, new HarshDeceiver(), "{3}{W}");

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new HorizonSeed());
        Permanent target = addCreatureReady(player1, new IsamaruHoundOfKonda());
        harness.castFromHand(player1, new SakuraTribeElder(), "{1}{G}");

        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isZero();
        harness.assertOnBattlefield(player1, "Sakura-Tribe Elder");
    }

    @Test
    @DisplayName("Casting an Arcane spell can regenerate an opponent's creature")
    void arcaneSpellCanRegenerateOpponentsCreature() {
        Permanent source = addCreatureReady(player1, new HorizonSeed());
        Permanent target = addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, source.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's Spirit spell does not trigger Horizon Seed")
    void opponentSpiritSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new HorizonSeed());
        Permanent target = addCreatureReady(player1, new IsamaruHoundOfKonda());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new HarshDeceiver(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isZero();
        harness.assertOnBattlefield(player2, "Harsh Deceiver");
    }
}
