package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.s.SiegeWurm;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({XathridSlyblade.class, SiegeWurm.class})
class XathridSlybladeTest extends BaseCardTest {

    @Test
    @DisplayName("Activating loses hexproof and gains first strike and deathtouch")
    void activationSwapsKeywords() {
        Permanent slyblade = addCreatureReady(player1, new XathridSlyblade());
        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.HEXPROOF)).isTrue();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("The keyword changes wear off at end of turn")
    void keywordChangesWearOff() {
        Permanent slyblade = addCreatureReady(player1, new XathridSlyblade());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void tappedSummoningSickSlybladeChangesKeywordsOnlyOnResolution() {
        harness.addToBattlefield(player1, new XathridSlyblade());
        Permanent slyblade = findPermanent(player1, "Xathrid Slyblade");
        slyblade.setSummoningSick(true);
        slyblade.tap();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.DEATHTOUCH)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, slyblade, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void activationOnlyChangesItsSource() {
        Permanent source = addCreatureReady(player1, new XathridSlyblade());
        Permanent other = addCreatureReady(player1, new XathridSlyblade());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, source, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void firstStrikeDeathtouchKillsLargerBlockerBeforeItCanDealDamage() {
        addCreatureReady(player1, new XathridSlyblade());
        addCreatureReady(player2, new SiegeWurm());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Xathrid Slyblade");
        harness.assertNotInGraveyard(player1, "Xathrid Slyblade");
        harness.assertInGraveyard(player2, "Siege Wurm");
        harness.assertNotOnBattlefield(player2, "Siege Wurm");
    }
}
