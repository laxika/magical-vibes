package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
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

@CardUsed({EzrimAgencyChief.class, LeoninScimitar.class})
class EzrimAgencyChiefTest extends BaseCardTest {

    @Test
    void entersAndInvestigatesTwice() {
        harness.setHand(player1, List.of(new EzrimAgencyChief()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        harness.clearPriorityPassed();
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
        Permanent ezrim = new Permanent(new EzrimAgencyChief());
        ezrim.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(ezrim);
        return ezrim;
    }
}
