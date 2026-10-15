package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.v.ValMaroonedSurveyor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EzrimAgencyChief.class, LeoninScimitar.class, ValMaroonedSurveyor.class})
class EzrimAgencyChiefTest extends BaseCardTest {

    @Test
    void entersAndInvestigatesTwice() {
        harness.setHand(player1, List.of(new EzrimAgencyChief()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    void sacrificingAnArtifactGrantsTheChosenKeywordUntilEndOfTurn() {
        Permanent ezrim = addReadyEzrim(player1);
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Hexproof");

        assertThat(gqs.hasKeyword(gd, ezrim, Keyword.HEXPROOF)).isTrue();
        harness.assertInGraveyard(player1, "Leonin Scimitar");

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ezrim, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void canChooseVigilance() {
        Permanent ezrim = addReadyEzrim(player1);
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Vigilance");
        assertThat(gqs.hasKeyword(gd, ezrim, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void canChooseLifelink() {
        Permanent ezrim = addReadyEzrim(player1);
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lifelink");

        assertThat(gqs.hasKeyword(gd, ezrim, Keyword.LIFELINK)).isTrue();
    }

    private Permanent addReadyEzrim(Player player) {
        Permanent ezrim = harness.addToBattlefieldAndReturn(player, new EzrimAgencyChief());
        ezrim.setSummoningSick(false);
        return ezrim;
    }

    @Test
    void investigatingTwiceTriggersEachInvestigationAbilityTwice() {
        harness.addToBattlefield(player1, new ValMaroonedSurveyor());
        harness.setHand(player1, List.of(new EzrimAgencyChief()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        int controllerLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLife + 4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 4);
    }

    @Test
    void cannotPayWithAnOpponentsArtifact() {
        addReadyEzrim(player1);
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanents(player2, "Leonin Scimitar")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateWhileTappedAndSummoningSickAndPaysSacrificeBeforeResolution() {
        Permanent ezrim = harness.addToBattlefieldAndReturn(player1, new EzrimAgencyChief());
        ezrim.setSummoningSick(true);
        ezrim.tap();
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Leonin Scimitar");
        assertThat(gqs.hasKeyword(gd, ezrim, Keyword.LIFELINK)).isFalse();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lifelink");

        assertThat(gqs.hasKeyword(gd, ezrim, Keyword.LIFELINK)).isTrue();
    }
}
