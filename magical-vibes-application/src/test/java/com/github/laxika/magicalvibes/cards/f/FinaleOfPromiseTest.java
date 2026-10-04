package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.a.AvenEternal;
import com.github.laxika.magicalvibes.cards.d.DiscoveryDispersal;
import com.github.laxika.magicalvibes.cards.j.JayasGreeting;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SparkHarvest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FinaleOfPromise.class, Shock.class, CounselOfTheSoratami.class, DiscoveryDispersal.class,
        JayasGreeting.class, SparkHarvest.class, AvenEternal.class})
class FinaleOfPromiseTest extends BaseCardTest {

    @Test
    void graveyardTargetsAreLimitedByXAndByCardType() {
        FinaleOfPromise finale = new FinaleOfPromise();
        Shock shock = new Shock();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(shock, counsel));

        ValidTargetsResponse instantTargets = harness.getValidTargetService()
                .computeValidTargetsForSpell(gd, finale, player1.getId(), null, 1);
        ValidTargetsResponse sorceryTargetsAtOne = harness.getValidTargetService()
                .computeValidTargetsForSpell(gd, finale, player1.getId(), List.of(shock.getId()), 1);
        ValidTargetsResponse sorceryTargetsAtThree = harness.getValidTargetService()
                .computeValidTargetsForSpell(gd, finale, player1.getId(), List.of(shock.getId()), 3);

        assertThat(instantTargets.validGraveyardCardIds()).containsExactly(shock.getId());
        assertThat(sorceryTargetsAtOne.validGraveyardCardIds()).isEmpty();
        assertThat(sorceryTargetsAtThree.validGraveyardCardIds()).containsExactly(counsel.getId());
    }

    @Test
    void splitInstantSorceryMayFillBothTargetSlots() {
        FinaleOfPromise finale = new FinaleOfPromise();
        DiscoveryDispersal splitCard = new DiscoveryDispersal();
        harness.setGraveyard(player1, List.of(splitCard));

        ValidTargetsResponse sorceryTargets = harness.getValidTargetService()
                .computeValidTargetsForSpell(gd, finale, player1.getId(), List.of(splitCard.getId()), 7);

        assertThat(sorceryTargets.validGraveyardCardIds()).containsExactly(splitCard.getId());
    }

    @Test
    void castsTheChosenInstantForFreeAndExilesIt() {
        FinaleOfPromise finale = new FinaleOfPromise();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(finale));
        harness.setGraveyard(player1, List.of(shock));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock).doesNotContain(finale);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(finale);
    }

    @Test
    void atTenCopiesTheChosenInstantTwice() {
        FinaleOfPromise finale = new FinaleOfPromise();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(finale));
        harness.setGraveyard(player1, List.of(shock));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 12);

        harness.castSorcery(player1, 0, 10, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock).doesNotContain(finale);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(finale);
    }

    @Test
    void mayResolveWithoutChoosingAnyTargetsAtZero() {
        FinaleOfPromise finale = new FinaleOfPromise();
        harness.setHand(player1, List.of(finale));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(finale);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(finale);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void atTenCopiesTheChosenSorceryTwice() {
        FinaleOfPromise finale = new FinaleOfPromise();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(finale));
        harness.setGraveyard(player1, List.of(counsel));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 12);

        harness.castSorcery(player1, 0, 10, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(counsel).doesNotContain(finale);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(finale);
    }

    @Test
    void belowTenDoesNotCopyTheChosenSpell() {
        FinaleOfPromise finale = new FinaleOfPromise();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(finale));
        harness.setGraveyard(player1, List.of(shock));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 11);

        harness.castSorcery(player1, 0, 9, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copiesWaitUntilBothChosenSpellsHaveBeenCast() {
        FinaleOfPromise finale = new FinaleOfPromise();
        Shock shock = new Shock();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(finale));
        harness.setGraveyard(player1, List.of(shock, counsel));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 12);

        harness.castSorcery(player1, 0, 10, List.of(shock.getId(), counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).filteredOn(entry -> entry.getCard() != finale).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(counsel);
    }

    @Test
    void mayChooseADifferentTargetForOneCopy() {
        FinaleOfPromise finale = new FinaleOfPromise();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(finale));
        harness.setGraveyard(player1, List.of(shock));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 12);

        harness.castSorcery(player1, 0, 10, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void mayDeclineBothChosenCardsEvenAtTen() {
        FinaleOfPromise finale = new FinaleOfPromise();
        Shock shock = new Shock();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(finale));
        harness.setGraveyard(player1, List.of(shock, counsel));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 12);

        harness.castSorcery(player1, 0, 10, List.of(shock.getId(), counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(finale, shock, counsel);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(finale, shock, counsel);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void aChosenSpellWithNoLegalTargetsStaysInTheGraveyard() {
        FinaleOfPromise finale = new FinaleOfPromise();
        JayasGreeting greeting = new JayasGreeting();
        harness.setHand(player1, List.of(finale));
        harness.setGraveyard(player1, List.of(greeting));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 2, List.of(greeting.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(greeting);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(greeting);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCastAChosenSpellWithoutPayingItsMandatoryAdditionalCost() {
        FinaleOfPromise finale = new FinaleOfPromise();
        SparkHarvest harvest = new SparkHarvest();
        AvenEternal creature = new AvenEternal();
        harness.addToBattlefield(player2, creature);
        harness.setHand(player1, List.of(finale));
        harness.setGraveyard(player1, List.of(harvest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 1, List.of(harvest.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(harvest);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(harvest);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == harvest);
    }
}
