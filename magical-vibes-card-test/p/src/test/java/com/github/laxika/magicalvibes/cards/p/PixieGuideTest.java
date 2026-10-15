package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ContactOtherPlane;
import com.github.laxika.magicalvibes.cards.f.FeywildTrickster;
import com.github.laxika.magicalvibes.cards.m.MinimusContainment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PixieGuide.class, ContactOtherPlane.class, FeywildTrickster.class, MinimusContainment.class})
class PixieGuideTest extends BaseCardTest {

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
    void rollsAnAdditionalDieAndUsesTheHighestResult() {
        setRolls(9, 10);
        harness.addToBattlefield(player1, new PixieGuide());
        harness.setLibrary(player1, List.of(new PixieGuide(), new PixieGuide(), new PixieGuide()));
        harness.setHand(player1, List.of(new ContactOtherPlane()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
    }

    @Test
    void ignoredDieDoesNotCauseAnAdditionalDiceTrigger() {
        setRolls(9, 10);
        harness.addToBattlefield(player1, new PixieGuide());
        harness.addToBattlefield(player1, new FeywildTrickster());
        harness.setLibrary(player1, List.of(new PixieGuide(), new PixieGuide(), new PixieGuide()));
        harness.setHand(player1, List.of(new ContactOtherPlane()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Faerie Dragon"))
                .hasSize(1);
    }

    @Test
    void multipleGuidesEachAddADie() {
        setRolls(1, 9, 20);
        harness.addToBattlefield(player1, new PixieGuide());
        harness.addToBattlefield(player1, new PixieGuide());
        harness.setLibrary(player1, List.of(new PixieGuide(), new PixieGuide(), new PixieGuide()));
        harness.setHand(player1, List.of(new ContactOtherPlane()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void opponentsGuideDoesNotModifyYourRoll() {
        setRolls(9, 20);
        harness.addToBattlefield(player2, new PixieGuide());
        harness.setLibrary(player1, List.of(new PixieGuide(), new PixieGuide(), new PixieGuide()));
        harness.setHand(player1, List.of(new ContactOtherPlane()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void guideThatLostItsAbilitiesDoesNotAddADie() {
        setRolls(9, 20);
        var guide = harness.addToBattlefieldAndReturn(player1, new PixieGuide());
        harness.setHand(player1, List.of(new MinimusContainment(), new ContactOtherPlane()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, guide.getId());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new PixieGuide(), new PixieGuide(), new PixieGuide()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private void setRolls(int... results) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService",
                new SequentialD20RollService(results));
    }

    private static final class SequentialD20RollService extends D20RollService {

        private final int[] results;
        private int index;

        private SequentialD20RollService(int[] results) {
            this.results = results;
        }

        @Override
        public int roll() {
            return results[Math.min(index++, results.length - 1)];
        }
    }
}
