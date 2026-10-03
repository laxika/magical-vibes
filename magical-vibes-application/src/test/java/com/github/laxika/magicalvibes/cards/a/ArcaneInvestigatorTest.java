package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.s.SilverRaven;
import com.github.laxika.magicalvibes.cards.d.DjinniWindseer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcaneInvestigator.class, HillGiantHerdgorger.class, SilverRaven.class, DjinniWindseer.class})
class ArcaneInvestigatorTest extends BaseCardTest {

    private RollD20EffectHandler rollHandler;
    private D20RollService originalRollService;

    @BeforeEach
    void captureRollService() {
        rollHandler = GameTestEngineContext.get().getBean(RollD20EffectHandler.class);
        originalRollService = (D20RollService) ReflectionTestUtils.getField(rollHandler, "d20RollService");
    }

    @AfterEach
    void restoreRollService() {
        ReflectionTestUtils.setField(rollHandler, "d20RollService", originalRollService);
    }

    @Test
    @DisplayName("Search the Room resolves a d20 result")
    void resolvesD20Result() {
        Card top = new HillGiantHerdgorger();
        Card second = new SilverRaven();
        Card third = new DjinniWindseer();
        harness.setLibrary(player1, List.of(top, second, third));
        harness.addToBattlefield(player1, new ArcaneInvestigator());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.LibraryRevealChoice) {
            harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.LibraryReorder reorder) {
                gs.handleInteractionAnswer(gd, player1,
                        new InteractionAnswer.CardOrder(List.of(
                                reorder.cards().indexOf(top), reorder.cards().indexOf(third))));
            }

            assertThat(gd.playerHands.get(player1.getId())).contains(second);
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, third);
        } else {
            assertThat(gd.playerHands.get(player1.getId())).contains(top);
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 9})
    void lowRollDrawsOnlyTheTopCard(int roll) {
        Card top = new SilverRaven();
        Card second = new HillGiantHerdgorger();
        harness.setLibrary(player1, List.of(top, second));

        activateWithRoll(roll);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 19, 20})
    void highRollChoosesOneAndOrdersTheRestBelowUntouchedCards(int roll) {
        Card top = new SilverRaven();
        Card second = new HillGiantHerdgorger();
        Card third = new DjinniWindseer();
        Card fourth = new SilverRaven();
        harness.setLibrary(player1, List.of(top, second, third, fourth));

        activateWithRoll(roll);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        PendingInteraction.LibraryReorder reorder = (PendingInteraction.LibraryReorder) gd.interaction.activeInteraction();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(
                List.of(reorder.cards().indexOf(third), reorder.cards().indexOf(top))));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, third, top);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void highRollWithOneCardPutsItIntoHandWithoutDrawing() {
        Card only = new SilverRaven();
        harness.setLibrary(player1, List.of(only));

        activateWithRoll(20);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void activateWithRoll(int roll) {
        ReflectionTestUtils.setField(rollHandler, "d20RollService", new D20RollService() {
            @Override
            public int roll() {
                return roll;
            }
        });
        harness.addToBattlefield(player1, new ArcaneInvestigator());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

}
