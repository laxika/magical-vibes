package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KasminaEnigmaticMentor.class, GrizzlyBears.class, Island.class, LightningBolt.class})
class KasminaEnigmaticMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's spell targeting a creature you control costs {2} more")
    void opponentSpellTargetingControlledCreatureCostsMore() {
        harness.addToBattlefield(player1, new KasminaEnigmaticMentor());
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareOpponentBolt();

        UUID creatureId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent's spell targeting a planeswalker you control costs {2} more")
    void opponentSpellTargetingControlledPlaneswalkerCostsMore() {
        harness.addToBattlefield(player1, new KasminaEnigmaticMentor());
        prepareOpponentBolt();

        UUID planeswalkerId = harness.getPermanentId(player1, "Kasmina, Enigmatic Mentor");

        assertThatThrownBy(() -> harness.castInstant(player2, 0, planeswalkerId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Kasmina does not tax your own spells")
    void doesNotTaxControllerSpells() {
        harness.addToBattlefield(player1, new KasminaEnigmaticMentor());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID creatureId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, creatureId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("-2 creates a Wizard and draws then discards a card")
    void minusTwoCreatesWizardAndLoots() {
        Permanent kasmina = addReadyKasmina(2);
        harness.setHand(player1, List.of(new Island()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent wizard = findPermanent(player1, "Wizard");
        assertThat(wizard.getCard().isToken()).isTrue();
        assertThat(wizard.getEffectivePower()).isEqualTo(2);
        assertThat(wizard.getEffectiveToughness()).isEqualTo(2);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Island");
        assertThat(kasmina.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    private void prepareOpponentBolt() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
    }

    private Permanent addReadyKasmina(int loyalty) {
        Permanent kasmina = harness.addToBattlefieldAndReturn(player1, new KasminaEnigmaticMentor());
        kasmina.setCounterCount(CounterType.LOYALTY, loyalty);
        kasmina.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return kasmina;
    }

    @Test
    void opponentCanPayTaxToTargetControlledCreature() {
        harness.addToBattlefield(player1, new KasminaEnigmaticMentor());
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareOpponentBolt();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Lightning Bolt");
    }

    @Test
    void opponentSpellTargetingPlayerIsNotTaxed() {
        harness.addToBattlefield(player1, new KasminaEnigmaticMentor());
        harness.setLife(player1, 20);
        prepareOpponentBolt();

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    void opponentSpellTargetingTheirOwnCreatureIsNotTaxed() {
        harness.addToBattlefield(player1, new KasminaEnigmaticMentor());
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareOpponentBolt();

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void minusTwoCanDiscardTheCardJustDrawn() {
        addReadyKasmina(5);
        harness.setHand(player1, List.of(new Island()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Wizard");
        assertThat(findPermanent(player1, "Kasmina, Enigmatic Mentor")
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }
}
