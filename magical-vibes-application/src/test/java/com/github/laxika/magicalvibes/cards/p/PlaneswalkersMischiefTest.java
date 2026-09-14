package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.ForsakenCity;
import com.github.laxika.magicalvibes.cards.i.Implode;
import com.github.laxika.magicalvibes.cards.s.Singe;
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

@CardUsed({PlaneswalkersMischief.class, Singe.class, ForsakenCity.class, Implode.class})
class PlaneswalkersMischiefTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a random instant or sorcery from the target opponent's hand")
    void exilesInstantOrSorceryFromOpponentsHand() {
        addMischief();
        Singe singe = new Singe();
        harness.setHand(player2, List.of(singe));
        addAbilityMana();

        activateMischief();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(singe);
    }

    @Test
    @DisplayName("Leaves a randomly revealed non-instant and non-sorcery in hand")
    void leavesNonSpellInHand() {
        addMischief();
        ForsakenCity city = new ForsakenCity();
        harness.setHand(player2, List.of(city));
        addAbilityMana();

        activateMischief();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(city);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does nothing when the target opponent has no cards in hand")
    void doesNothingForEmptyHand() {
        addMischief();
        harness.setHand(player2, List.of());
        addAbilityMana();

        activateMischief();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Lets its controller cast an exiled sorcery for free even after it leaves the battlefield")
    void castsExiledSorceryForFree() {
        Permanent mischief = addMischief();
        Implode implode = new Implode();
        Permanent city = harness.addToBattlefieldAndReturn(player2, new ForsakenCity());
        ForsakenCity drawnCard = new ForsakenCity();
        harness.setHand(player2, List.of(implode));
        harness.setLibrary(player1, List.of(drawnCard));
        addAbilityMana();

        activateMischief();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, mischief));
        harness.castFromExile(player1, implode.getId(), city.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(implode);
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(
                permanent -> permanent.getId().equals(city.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(implode);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Returns an uncast exiled spell to its owner's hand at the next end step")
    void returnsUncastSpellAtNextEndStep() {
        addMischief();
        Singe singe = new Singe();
        harness.setHand(player2, List.of(singe));
        addAbilityMana();

        activateMischief();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(singe);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(singe);
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetController() {
        addMischief();
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only its controller may cast the exiled spell")
    void onlyItsControllerMayCastExiledSpell() {
        addMischief();
        Implode implode = new Implode();
        Permanent city = harness.addToBattlefieldAndReturn(player2, new ForsakenCity());
        harness.setHand(player2, List.of(implode));
        addAbilityMana();

        activateMischief();

        assertThatThrownBy(() -> harness.castFromExile(player2, implode.getId(), city.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(implode);
    }

    @Test
    @DisplayName("Can activate only at sorcery speed")
    void cannotActivateOutsideSorcerySpeed() {
        addMischief();
        addAbilityMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addMischief() {
        return harness.addToBattlefieldAndReturn(player1, new PlaneswalkersMischief());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void activateMischief() {
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
    }
}
