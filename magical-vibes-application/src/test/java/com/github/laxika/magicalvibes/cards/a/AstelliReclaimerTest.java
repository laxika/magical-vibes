package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarksteelForge;
import com.github.laxika.magicalvibes.cards.d.DoubleMajor;
import com.github.laxika.magicalvibes.cards.e.EmergencyEject;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InfiniteGuidelineStation;
import com.github.laxika.magicalvibes.cards.p.PlasmaBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AstelliReclaimer.class, DarksteelForge.class, Forest.class, GildedLotus.class,
        GrizzlyBears.class, AllFatesScroll.class, InfiniteGuidelineStation.class,
        EmergencyEject.class, PlasmaBolt.class, DoubleMajor.class})
class AstelliReclaimerTest extends BaseCardTest {

    @Test
    void returnsEligiblePermanentWithinManaSpentLimit() {
        Card eligible = new GildedLotus();
        Card tooExpensive = new DarksteelForge();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive));
        castAstelli();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gilded Lotus");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(tooExpensive);
    }

    @Test
    void doesNotTargetCreaturesOrLands() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, land));
        castAstelli();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, land);
    }

    @Test
    void warpReturnsThreeManaPermanentButNotFiveManaPermanent() {
        Card eligible = new AllFatesScroll();
        Card tooExpensive = new InfiniteGuidelineStation();
        AstelliReclaimer reclaimer = new AstelliReclaimer();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive));
        harness.setHand(player1, List.of(reclaimer));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "All-Fates Scroll");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(tooExpensive);

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(reclaimer.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Astelli Reclaimer");
        harness.assertOnBattlefield(player1, "All-Fates Scroll");
    }

    @Test
    void copiedSpellCannotTargetCardsUsingManaSpentOnTheOriginal() {
        Card artifact = new AllFatesScroll();
        AstelliReclaimer reclaimer = new AstelliReclaimer();
        Card doubleMajor = new DoubleMajor();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(reclaimer, doubleMajor));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.castInstant(player1, 0, reclaimer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact, doubleMajor);
        harness.assertNotOnBattlefield(player1, "All-Fates Scroll");

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "All-Fates Scroll");
    }

    @Test
    void canRespondToWarpExileTriggerAtTheBeginningOfTheEndStep() {
        AstelliReclaimer reclaimer = new AstelliReclaimer();
        harness.setHand(player1, List.of(reclaimer));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Astelli Reclaimer");
        assertThat(gd.findExiledCard(reclaimer.getId())).isNull();
        harness.setHand(player2, List.of(new EmergencyEject()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Astelli Reclaimer"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Astelli Reclaimer");
        assertThat(gd.findExiledCard(reclaimer.getId())).isNull();
    }

    @Test
    void enteringWithoutBeingCastCannotReturnPositiveManaValueCard() {
        Card artifact = new AllFatesScroll();
        harness.setGraveyard(player1, List.of(artifact));

        harness.enterBattlefieldAndReturn(player1, new AstelliReclaimer());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact);
        harness.assertNotOnBattlefield(player1, "All-Fates Scroll");
    }

    @Test
    void cannotTargetNonpermanentCardsOrOpponentsGraveyard() {
        Card sorcery = new PlasmaBolt();
        Card opponentsArtifact = new AllFatesScroll();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setGraveyard(player2, List.of(opponentsArtifact));

        castAstelli();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsArtifact);
        harness.assertNotOnBattlefield(player1, "All-Fates Scroll");
    }

    @Test
    void returnsTargetEvenIfReclaimerLeavesBeforeAbilityResolves() {
        Card artifact = new AllFatesScroll();
        harness.setGraveyard(player1, List.of(artifact));
        castAstelli();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        harness.setHand(player2, List.of(new EmergencyEject()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Astelli Reclaimer"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Astelli Reclaimer");
        harness.assertNotOnBattlefield(player1, "All-Fates Scroll");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "All-Fates Scroll");
        harness.assertNotInGraveyard(player1, "All-Fates Scroll");
    }

    private void castAstelli() {
        harness.setHand(player1, List.of(new AstelliReclaimer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
