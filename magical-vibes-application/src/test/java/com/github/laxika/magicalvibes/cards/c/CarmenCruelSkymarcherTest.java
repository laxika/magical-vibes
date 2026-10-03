package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.v.VisceraSeer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CarmenCruelSkymarcher.class, GrizzlyBears.class, HillGiant.class,
        KuldothaRebirth.class, Spellbook.class, VisceraSeer.class})
class CarmenCruelSkymarcherTest extends BaseCardTest {

    @Test
    @DisplayName("A sacrificed permanent puts a counter on Carmen and gains 1 life")
    void sacrificedPermanentGrowsCarmenAndGainsLife() {
        Permanent carmen = addReadyCarmen(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setLife(player1, 10);

        castKuldothaRebirth(player1, artifact);
        harness.passBothPriorities();

        assertThat(carmen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("Attacking returns an eligible permanent card from the graveyard")
    void attackReturnsPermanentWithinCarmensPower() {
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive));
        addReadyCarmen(player1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    private void castKuldothaRebirth(com.github.laxika.magicalvibes.model.Player player,
                                     Permanent artifact) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new KuldothaRebirth()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.castSorceryWithSacrifice(player, 0, artifact.getId());
    }

    private Permanent addReadyCarmen(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new CarmenCruelSkymarcher());
    }

    @Test
    @DisplayName("An opponent's sacrifice grows Carmen and gains life for Carmen's controller")
    void opponentSacrificeGainsLifeForController() {
        Permanent carmen = addReadyCarmen(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        castKuldothaRebirth(player2, artifact);
        harness.passBothPriorities();

        assertThat(carmen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 11);
        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("Sacrificing Carmen herself still gains 1 life")
    void sacrificingCarmenStillGainsLife() {
        Permanent carmen = addReadyCarmen(player1);
        addCreatureReady(player1, new VisceraSeer());
        harness.setLife(player1, 10);
        harness.setLibrary(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, carmen.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Carmen, Cruel Skymarcher");
        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("The attack can return a noncreature permanent but cannot target a sorcery")
    void attackReturnsArtifactAndExcludesSorcery() {
        Card artifact = new Spellbook();
        Card sorcery = new KuldothaRebirth();
        harness.setGraveyard(player1, List.of(artifact, sorcery));
        harness.setGraveyard(player2, List.of(new Spellbook()));
        addReadyCarmen(player1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Kuldotha Rebirth");
        harness.assertInGraveyard(player2, "Spellbook");
    }

    @Test
    @DisplayName("Carmen's attack allows choosing no target even when one is eligible")
    void attackCanChooseNoTarget() {
        harness.setGraveyard(player1, List.of(new VisceraSeer()));
        addReadyCarmen(player1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Viscera Seer");
        harness.assertNotOnBattlefield(player1, "Viscera Seer");
    }

    @Test
    @DisplayName("A graveyard target becomes illegal if Carmen's power drops before resolution")
    void targetBecomesIllegalWhenPowerDrops() {
        Card target = new HillGiant();
        harness.setGraveyard(player1, List.of(target));
        Permanent carmen = addReadyCarmen(player1);
        carmen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        carmen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }
}
