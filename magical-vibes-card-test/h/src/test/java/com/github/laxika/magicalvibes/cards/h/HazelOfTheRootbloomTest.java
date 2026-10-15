package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HazelOfTheRootbloom.class)
class HazelOfTheRootbloomTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping X tokens and paying 2 life adds X mana in independently chosen colors")
    void tapsTokensForMana() {
        Permanent hazel = addCreatureReady(player1, new HazelOfTheRootbloom());
        Permanent firstToken = addToken(false);
        Permanent secondToken = addToken(false);
        Permanent tappedToken = addToken(false);
        tappedToken.tap();

        harness.activateAbility(player1, 0, 0, 2, null);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(hazel.isTapped()).isTrue();
        assertThat(firstToken.isTapped()).isTrue();
        assertThat(secondToken.isTapped()).isTrue();
        assertThat(tappedToken.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("At the beginning of the end step, a non-Squirrel token gets copied once")
    void copiesNonSquirrelTokenOnce() {
        addCreatureReady(player1, new HazelOfTheRootbloom());
        Permanent token = addToken(false);

        resolveEndStepTriggerTarget(token);

        assertThat(tokenCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("At the beginning of the end step, a Squirrel token gets copied twice")
    void copiesSquirrelTokenTwice() {
        addCreatureReady(player1, new HazelOfTheRootbloom());
        Permanent squirrel = addToken(true);

        resolveEndStepTriggerTarget(squirrel);

        assertThat(tokenCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("The end-step ability only allows targeting a token you control")
    void endStepAbilityOnlyTargetsOwnTokens() {
        addCreatureReady(player1, new HazelOfTheRootbloom());
        Permanent token = addToken(false);
        Permanent nontoken = addCreatureReady(player1, new Card() {
        });

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(token.getId())
                .doesNotContain(nontoken.getId());
    }

    private Permanent addToken(boolean squirrel) {
        Card card = new Card() {
        };
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(squirrel ? List.of(CardSubtype.SQUIRREL) : List.of());
        card.setToken(true);
        return addCreatureReady(player1, card);
    }

    private void resolveEndStepTriggerTarget(Permanent target) {
        advanceToEndStep();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private long tokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
    }
}
