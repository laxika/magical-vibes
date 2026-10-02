package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.r.RealityShift;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.effect.normalfx.ArcaneEndeavorEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiceRollService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcaneEndeavor.class, Divination.class, Forest.class, GrizzlyBears.class, Opt.class, RealityShift.class})
class ArcaneEndeavorTest extends BaseCardTest {

    private ArcaneEndeavorEffectHandler effectHandler;
    private DiceRollService originalDiceRollService;

    @BeforeEach
    void captureDiceRollService() {
        effectHandler = GameTestEngineContext.get().getBean(ArcaneEndeavorEffectHandler.class);
        originalDiceRollService = (DiceRollService) ReflectionTestUtils.getField(effectHandler, "diceRollService");
    }

    @AfterEach
    void restoreDiceRollService() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", originalDiceRollService);
    }

    @Test
    void chosenResultDrawsThatManyAndOtherResultCapsInstantOrSorceryCast() {
        ArcaneEndeavor endeavor = new ArcaneEndeavor();
        Opt eligibleSpell = new Opt();
        Divination tooExpensiveSpell = new Divination();
        GrizzlyBears creature = new GrizzlyBears();
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(2, 1));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(endeavor, eligibleSpell, tooExpensiveSpell, creature));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "2");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5)
                .contains(eligibleSpell, tooExpensiveSpell, creature);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(eligibleSpell);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(eligibleSpell);
    }

    @Test
    void choosingTheOtherResultDrawsTheOtherNumber() {
        ArcaneEndeavor endeavor = new ArcaneEndeavor();
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(3, 6));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.castFromHand(player1, endeavor, "{5}{U}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "6");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void equalRollsDrawAndAllowCastingWithoutANumberChoice() {
        Divination drawnSpell = new Divination();
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(3, 3));
        harness.setLibrary(player1, List.of(drawnSpell, new Forest(), new Forest(), new Forest(), new Forest()));

        harness.castFromHand(player1, new ArcaneEndeavor(), "{5}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).contains(drawnSpell);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(drawnSpell);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4).doesNotContain(drawnSpell);
        harness.assertInGraveyard(player1, "Divination");
    }

    @Test
    void mayDeclineCastingAfterDrawing() {
        Opt drawnSpell = new Opt();
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(1, 1));
        harness.setLibrary(player1, List.of(drawnSpell, new Forest()));

        harness.castFromHand(player1, new ArcaneEndeavor(), "{5}{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnSpell);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Arcane Endeavor");
    }

    @Test
    void acceptingOneSpellDoesNotAllowCastingASecondSpell() {
        Divination firstSpell = new Divination();
        Opt secondSpell = new Opt();
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(2, 3));
        harness.setLibrary(player1, List.of(firstSpell, secondSpell, new Forest(), new Forest()));

        harness.castFromHand(player1, new ArcaneEndeavor(), "{5}{U}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "2");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(firstSpell);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondSpell);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).contains(secondSpell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({ArcaneEndeavor.class, RealityShift.class, Forest.class})
    void spellWithNoLegalTargetsRemainsInHand() {
        RealityShift uncastableSpell = new RealityShift();
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(1, 2));
        harness.setLibrary(player1, List.of(uncastableSpell, new Forest()));

        harness.castFromHand(player1, new ArcaneEndeavor(), "{5}{U}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1");
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(uncastableSpell);
        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Reality Shift");
    }

    private static final class FixedDiceRollService extends DiceRollService {

        private final int[] results;
        private int index;

        private FixedDiceRollService(int... results) {
            this.results = results;
        }

        @Override
        public int roll(int sides) {
            return results[Math.min(index++, results.length - 1)];
        }
    }
}
