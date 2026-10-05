package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AltanakTheThriceCalled;
import com.github.laxika.magicalvibes.cards.c.CautiousSurvivor;
import com.github.laxika.magicalvibes.cards.f.FearOfLostTeeth;
import com.github.laxika.magicalvibes.cards.g.GrabThePrize;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.u.UnableToScream;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImpossibleInferno.class, AltanakTheThriceCalled.class, CautiousSurvivor.class, Mountain.class,
        UnableToScream.class, GrabThePrize.class, FearOfLostTeeth.class})
class ImpossibleInfernoTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 6 damage to target creature")
    void dealsSixDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());

        cast(target);

        harness.assertNotOnBattlefield(player2, "Cautious Survivor");
    }

    @Test
    @DisplayName("Delirium exiles the top card and allows it to be played until the end of next turn")
    void deliriumExilesTopCardAndGrantsPlayPermission() {
        harness.setGraveyard(player1, List.of(new CautiousSurvivor(), new GrabThePrize(), new Mountain(),
                new UnableToScream()));
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());

        cast(target);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Without delirium, it does not exile the top card")
    void withoutDeliriumDoesNotExileTopCard() {
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());

        cast(target);

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        harness.setHand(player1, List.of(new ImpossibleInferno()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dealsExactlySixDamageToSurvivingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AltanakTheThriceCalled());

        cast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(6);
        harness.assertOnBattlefield(player1, "Altanak, the Thrice-Called");
    }

    @Test
    void resolvingInfernoDoesNotCountItselfForDelirium() {
        harness.setGraveyard(player1, List.of(new CautiousSurvivor(), new Mountain(), new UnableToScream()));
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());

        cast(target);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        harness.assertInGraveyard(player1, "Impossible Inferno");
    }

    @Test
    void lethallyDamagedCreatureDoesNotEnterGraveyardBeforeDeliriumCheck() {
        harness.setGraveyard(player1, List.of(new GrabThePrize(), new Mountain(), new UnableToScream()));
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CautiousSurvivor());

        cast(target);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertInGraveyard(player1, "Cautious Survivor");
    }

    @Test
    void illegalOnlyTargetPreventsDeliriumExile() {
        enableDelirium();
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());
        prepareCast();
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void deliriumWithEmptyLibraryStillDealsDamage() {
        enableDelirium();
        harness.setLibrary(player1, List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());

        cast(target);

        harness.assertInGraveyard(player2, "Cautious Survivor");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void exiledLandCanBePlayed() {
        enableDelirium();
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());

        cast(target);
        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void permissionLastsThroughNextTurnEndStepThenExpires() {
        enableDelirium();
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard, new Mountain(), new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());

        cast(target);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    void exiledCreatureRequiresItsNormalManaCost() {
        enableDelirium();
        Card topCard = new CautiousSurvivor();
        harness.setLibrary(player1, List.of(topCard));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());

        cast(target);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cautious Survivor");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void oneCardCanContributeTwoTypesToDelirium() {
        harness.setGraveyard(player1, List.of(new FearOfLostTeeth(), new GrabThePrize(), new Mountain()));
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());

        cast(target);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    void fourCardsWithOnlyTwoTypesDoNotEnableDelirium() {
        harness.setGraveyard(player1, List.of(new CautiousSurvivor(), new CautiousSurvivor(),
                new Mountain(), new Mountain()));
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());

        cast(target);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void deliriumIsCheckedAtResolutionRatherThanCasting() {
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CautiousSurvivor());
        prepareCast();
        harness.castInstant(player1, 0, target.getId());
        enableDelirium();

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    private void enableDelirium() {
        harness.setGraveyard(player1, List.of(new CautiousSurvivor(), new GrabThePrize(),
                new Mountain(), new UnableToScream()));
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new ImpossibleInferno()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void cast(Permanent target) {
        prepareCast();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
