package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CanoptekWraith;
import com.github.laxika.magicalvibes.cards.d.DreadReturn;
import com.github.laxika.magicalvibes.cards.i.IlluminorSzeras;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostArk.class, GrizzlyBears.class, Ornithopter.class, MindStone.class,
        CanoptekWraith.class, DreadReturn.class, IlluminorSzeras.class})
class GhostArkTest extends BaseCardTest {

    @Test
    void grantsUnearthToArtifactCreatureCardsOnly() {
        CardSetup cards = setUpBattlefieldAndGraveyard();

        crewGhostArk(cards.ark());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateGraveyardAbility(player1, 2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(cards.artifactCreature().getId()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unearthGrantEndsAtEndOfTurn() {
        CardSetup cards = setUpBattlefieldAndGraveyard();

        crewGhostArk(cards.ark());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unearthedCreatureHasHasteAndIsExiledAtNextEndStep() {
        CardSetup cards = setUpBattlefieldAndGraveyard();
        crewGhostArk(cards.ark());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateGraveyardAbility(player1, 2);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cards.artifactCreature().getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(returned);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(cards.artifactCreature());
    }

    @Test
    void doesNotGrantUnearthToOpponentsGraveyard() {
        Permanent ark = addCreatureReady(player1, new GhostArk());
        addCreatureReady(player1, new CanoptekWraith());
        harness.setGraveyard(player2, List.of(new CanoptekWraith()));
        crewGhostArk(ark);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantsUnearthToCardsPresentWhenTriggerResolves() {
        Permanent ark = addCreatureReady(player1, new GhostArk());
        addCreatureReady(player1, new CanoptekWraith());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        CanoptekWraith card = new CanoptekWraith();
        harness.setGraveyard(player1, List.of(card));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
        assertThat(gqs.isCreature(gd, ark)).isTrue();
    }

    @Test
    void laterCardsNeedAnotherCrewResolutionToGainUnearth() {
        Permanent ark = addCreatureReady(player1, new GhostArk());
        addCreatureReady(player1, new CanoptekWraith());
        crewGhostArk(ark);

        CanoptekWraith card = new CanoptekWraith();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        addCreatureReady(player1, new CanoptekWraith());
        crewGhostArk(ark);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
    }

    @Test
    void grantedUnearthRequiresThreeManaAndSorceryTiming() {
        CardSetup cards = setUpBattlefieldAndGraveyard();
        crewGhostArk(cards.ark());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 2))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantIsLostWhenCardLeavesGraveyardAndReturns() {
        CanoptekWraith card = new CanoptekWraith();
        harness.setGraveyard(player1, List.of(card));
        Permanent ark = addCreatureReady(player1, new GhostArk());
        addCreatureReady(player1, new CanoptekWraith());
        crewGhostArk(ark);

        harness.setHand(player1, List.of(new DreadReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, card.getId());
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst().orElseThrow();
        Permanent szeras = addCreatureReady(player1, new IlluminorSzeras());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(szeras), null, null);
        harness.handlePermanentChosen(player1, returned.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        int cardIndex = gd.playerGraveyards.get(player1.getId()).indexOf(card);
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, cardIndex))
                .isInstanceOf(IllegalStateException.class);
    }

    private CardSetup setUpBattlefieldAndGraveyard() {
        Ornithopter artifactCreature = new Ornithopter();
        GrizzlyBears nonArtifactCreature = new GrizzlyBears();
        MindStone artifact = new MindStone();
        harness.setGraveyard(player1, List.of(nonArtifactCreature, artifact, artifactCreature));

        Permanent ark = addCreatureReady(player1, new GhostArk());
        addCreatureReady(player1, new GrizzlyBears());
        return new CardSetup(artifactCreature, ark);
    }

    private void crewGhostArk(Permanent ark) {
        int arkIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ark);
        harness.activateAbility(player1, arkIndex, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private record CardSetup(Ornithopter artifactCreature, Permanent ark) {
    }
}
