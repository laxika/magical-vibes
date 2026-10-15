package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.o.OnakkeOgre;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WildwoodScourge;
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

@CardUsed({IdolOfEndurance.class, OnakkeOgre.class, ColossalDreadmaw.class, Shock.class, WildwoodScourge.class})
class IdolOfEnduranceTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles qualifying creature cards and returns them to the graveyard when it leaves")
    void exilesQualifyingCreaturesUntilItLeaves() {
        Card cheapCreature = new OnakkeOgre();
        Card expensiveCreature = new ColossalDreadmaw();
        Card nonCreature = new Shock();
        harness.setGraveyard(player1, List.of(cheapCreature, expensiveCreature, nonCreature));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new IdolOfEndurance(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent idol = findPermanent(player1, "Idol of Endurance");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(cheapCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(expensiveCreature, nonCreature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, idol));

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(expensiveCreature, nonCreature, idol.getCard(), cheapCreature);
    }

    @Test
    @DisplayName("Activated ability grants one later free creature cast from the Idol's exile")
    void activatedAbilityGrantsOneFreeCreatureCast() {
        Permanent idol = addReadyIdol();
        Card firstCreature = new OnakkeOgre();
        Card secondCreature = new OnakkeOgre();
        gd.addToExile(player1.getId(), firstCreature, idol.getId());
        gd.addToExile(player1.getId(), secondCreature, idol.getId());
        addActivationMana();

        activateIdol(idol);
        harness.castFromExile(player1, firstCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(firstCreature.getId()));
        assertThatThrownBy(() -> harness.castFromExile(player1, secondCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activated ability only permits creature cards and expires at end of turn")
    void activatedAbilityFiltersCardsAndExpires() {
        Permanent idol = addReadyIdol();
        Card creature = new OnakkeOgre();
        Card nonCreature = new Shock();
        gd.addToExile(player1.getId(), creature, idol.getId());
        gd.addToExile(player1.getId(), nonCreature, idol.getId());
        addActivationMana();

        activateIdol(idol);

        assertThatThrownBy(() -> harness.castFromExile(player1, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A card that returns to exile is no longer linked to the Idol")
    void cardThatReturnsToExileIsNotReturnedWithIdol() {
        Card creature = new OnakkeOgre();
        harness.setGraveyard(player1, List.of(creature));
        Permanent idol = castAndResolveIdol();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);

        gd.removeFromExile(creature.getId());
        gd.addToExile(player1.getId(), creature);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, idol));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Nothing is exiled if Idol leaves before its enters trigger resolves")
    void sourceLeavesBeforeEntersTriggerResolves() {
        Card creature = new OnakkeOgre();
        harness.setGraveyard(player1, List.of(creature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new IdolOfEndurance(), "{2}{W}");
        harness.passBothPriorities();
        Permanent idol = findPermanent(player1, "Idol of Endurance");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, idol));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature, idol.getCard());
    }

    @Test
    @DisplayName("The enters trigger exiles all qualifying cards only from its controller's graveyard")
    void exilesAllQualifyingCardsOnlyFromOwnGraveyard() {
        Card first = new OnakkeOgre();
        Card second = new OnakkeOgre();
        Card opposing = new OnakkeOgre();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(opposing));

        castAndResolveIdol();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposing);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The permission preserves creature timing and can be used later in the main phase")
    void permissionDoesNotGrantFlash() {
        Permanent idol = addReadyIdol();
        Card creature = new OnakkeOgre();
        gd.addToExile(player1.getId(), creature, idol.getId());
        addActivationMana();
        activateIdol(idol);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Onakke Ogre");
    }

    @Test
    @DisplayName("A grant cannot cast cards exiled with another Idol or by an unrelated effect")
    void permissionOnlyUsesThisIdolsCards() {
        Permanent idol = addReadyIdol();
        Permanent otherIdol = addReadyIdol();
        Card linked = new OnakkeOgre();
        Card otherLinked = new OnakkeOgre();
        Card unrelated = new OnakkeOgre();
        gd.addToExile(player1.getId(), linked, idol.getId());
        gd.addToExile(player1.getId(), otherLinked, otherIdol.getId());
        gd.addToExile(player1.getId(), unrelated);
        addActivationMana();
        activateIdol(idol);

        assertThatThrownBy(() -> harness.castFromExile(player1, otherLinked.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromExile(player1, unrelated.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromExile(player2, linked.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.castFromExile(player1, linked.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Onakke Ogre");
    }

    @Test
    @DisplayName("Idol leaving returns uncast cards but leaves a cast creature on the battlefield")
    void returnsOnlyCardsStillInExile() {
        Card castCreature = new OnakkeOgre();
        Card remainingCreature = new OnakkeOgre();
        harness.setGraveyard(player1, List.of(castCreature, remainingCreature));
        Permanent idol = castAndResolveIdol();
        addActivationMana();
        activateIdol(idol);
        harness.castFromExile(player1, castCreature.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, idol));

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(idol.getCard(), remainingCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(castCreature.getId()));
    }

    @Test
    @DisplayName("Two resolved activations each allow one creature spell")
    void repeatedActivationsGrantSeparateCasts() {
        Permanent idol = addReadyIdol();
        Card first = new OnakkeOgre();
        Card second = new OnakkeOgre();
        Card third = new OnakkeOgre();
        gd.addToExile(player1.getId(), first, idol.getId());
        gd.addToExile(player1.getId(), second, idol.getId());
        gd.addToExile(player1.getId(), third, idol.getId());
        addActivationMana();
        activateIdol(idol);
        idol.untap();
        addActivationMana();
        activateIdol(idol);

        harness.castFromExile(player1, first.getId());
        harness.passBothPriorities();
        harness.castFromExile(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(second.getId()));
        assertThatThrownBy(() -> harness.castFromExile(player1, third.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An X-cost creature qualifies in the graveyard and must be cast with X equal to zero")
    void xCostCreatureIsExiledAndCastWithZeroX() {
        Card creature = new WildwoodScourge();
        harness.setGraveyard(player1, List.of(creature));
        Permanent idol = castAndResolveIdol();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        addActivationMana();
        activateIdol(idol);

        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wildwood Scourge");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private Permanent addReadyIdol() {
        return harness.addToBattlefieldAndReturn(player1, new IdolOfEndurance());
    }

    private Permanent castAndResolveIdol() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new IdolOfEndurance(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Idol of Endurance");
    }

    private void addActivationMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void activateIdol(Permanent idol) {
        int idolIndex = gd.playerBattlefields.get(player1.getId()).indexOf(idol);
        harness.activateAbility(player1, idolIndex, null, null);
        harness.passBothPriorities();
    }
}
