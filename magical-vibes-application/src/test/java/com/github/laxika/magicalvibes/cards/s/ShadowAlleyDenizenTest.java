package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadowAlleyDenizen.class, GrizzlyBears.class, WalkingCorpse.class, Forest.class})
class ShadowAlleyDenizenTest extends BaseCardTest {

    @Test
    @DisplayName("Another black creature entering lets you give target creature intimidate")
    void blackCreatureEnterGrantsIntimidate() {
        harness.addToBattlefield(player1, new ShadowAlleyDenizen());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new WalkingCorpse(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature is a legal target")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player1, new ShadowAlleyDenizen());
        Permanent enemy = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new WalkingCorpse(), "{1}{B}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, enemy.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enemy, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("Intimidate wears off at end of turn")
    void intimidateWearsOff() {
        harness.addToBattlefield(player1, new ShadowAlleyDenizen());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new WalkingCorpse(), "{1}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("A non-black creature entering does not trigger the ability")
    void nonBlackCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ShadowAlleyDenizen());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Shadow Alley Denizen entering does not trigger its own ability")
    void ownEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new ShadowAlleyDenizen(), "{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's black creature entering does not trigger")
    void opponentBlackCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ShadowAlleyDenizen());
        harness.enterBattlefieldAndReturn(player2, new ShadowAlleyDenizen());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Denizen can target itself when another black creature enters")
    void canTargetItself() {
        Permanent denizen = harness.addToBattlefieldAndReturn(player1, new ShadowAlleyDenizen());
        harness.castFromHand(player1, new ShadowAlleyDenizen(), "{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, denizen.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, denizen, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("The entering black creature is a legal target")
    void canTargetEnteringCreature() {
        harness.addToBattlefield(player1, new ShadowAlleyDenizen());
        ShadowAlleyDenizen enteringCard = new ShadowAlleyDenizen();
        harness.castFromHand(player1, enteringCard, "{B}");
        harness.passBothPriorities();
        Permanent entering = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == enteringCard)
                .findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, entering.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, entering, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("Noncreature lands are not legal targets for the trigger")
    void cannotTargetNoncreatureLand() {
        Permanent denizen = harness.addToBattlefieldAndReturn(player1, new ShadowAlleyDenizen());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new ShadowAlleyDenizen(), "{B}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(denizen.getId()).doesNotContain(forest.getId());
    }
}
