package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({Revivify.class, GrizzlyBears.class, Shock.class})
class RevivifyTest extends BaseCardTest {

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
    void lowRollReturnsCreatureCardsToHand() {
        Card creature = putCreatureIntoGraveyard();
        setRoll(1);

        castRevivify();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
    }

    @Test
    void fourteenRollPlusQualifyingCreatureReturnsItToBattlefield() {
        Card creature = putCreatureIntoGraveyard();
        setRoll(14);

        castRevivify();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    void returnsAllQualifyingCreaturesOnHighRoll() {
        Card firstCreature = putCreatureIntoGraveyard();
        Card secondCreature = new GrizzlyBears();
        Permanent secondPermanent = harness.addToBattlefieldAndReturn(player1, secondCreature);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, secondPermanent.getId());
        harness.passBothPriorities();

        setRoll(15);
        castRevivify();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(firstCreature.getId(), secondCreature.getId());
    }

    private Card putCreatureIntoGraveyard() {
        Card creature = new GrizzlyBears();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, creature);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, permanent.getId());
        harness.passBothPriorities();
        return creature;
    }

    private void castRevivify() {
        harness.setHand(player1, List.of(new Revivify()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
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
