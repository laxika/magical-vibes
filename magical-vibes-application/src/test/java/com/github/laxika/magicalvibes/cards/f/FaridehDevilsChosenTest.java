package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ContactOtherPlane;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaridehDevilsChosen.class, ContactOtherPlane.class, GrizzlyBears.class})
class FaridehDevilsChosenTest extends BaseCardTest {

    private RollD20EffectHandler rollD20EffectHandler;
    private D20RollService originalD20RollService;

    @BeforeEach
    void captureD20RollService() {
        rollD20EffectHandler = GameTestEngineContext.get().getBean(RollD20EffectHandler.class);
        originalD20RollService = (D20RollService) ReflectionTestUtils.getField(
                rollD20EffectHandler, "d20RollService");
    }

    @AfterEach
    void restoreD20RollService() {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", originalD20RollService);
    }

    @Test
    void rollingNineGrantsKeywordsButDoesNotDraw() {
        setRoll(9);
        Card libraryCard = new GrizzlyBears();
        Permanent farideh = prepare(libraryCard);

        castRollSpell();

        assertThat(gqs.hasKeyword(gd, farideh, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, farideh, Keyword.MENACE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void rollingTenOrHigherAlsoDrawsACard() {
        setRoll(10);
        Card libraryCard = new GrizzlyBears();
        Permanent farideh = prepare(libraryCard);

        castRollSpell();

        assertThat(gqs.hasKeyword(gd, farideh, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, farideh, Keyword.MENACE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).contains(libraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void grantedKeywordsWearOffAtEndOfTurn() {
        setRoll(9);
        Permanent farideh = prepare(new GrizzlyBears());

        castRollSpell();

        assertThat(gqs.hasKeyword(gd, farideh, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, farideh, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, farideh, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, farideh, Keyword.MENACE)).isFalse();
    }

    private Permanent prepare(Card libraryCard) {
        Permanent farideh = harness.addToBattlefieldAndReturn(player1, new FaridehDevilsChosen());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), libraryCard));
        harness.setHand(player1, List.of(new ContactOtherPlane()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        return farideh;
    }

    private void castRollSpell() {
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.Scry.class) != null) {
            gs.handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        }
        harness.passBothPriorities();
    }

    private void setRoll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
    }

    private static final class FixedD20RollService extends D20RollService {

        private final int result;

        private FixedD20RollService(int result) {
            this.result = result;
        }

        @Override
        public int roll() {
            return result;
        }
    }
}
