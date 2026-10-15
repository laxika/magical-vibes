package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
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

@CardUsed({Revivify.class, GrizzlyBears.class, Shock.class, SoulWarden.class})
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
        Card secondCreature = putCreatureIntoGraveyard();

        setRoll(15);
        castRevivify();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(firstCreature.getId(), secondCreature.getId());
    }

    @Test
    void modifiedRollOfFourteenReturnsToHand() {
        Card creature = putCreatureIntoGraveyard();
        setRoll(13);

        castRevivify();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .doesNotContain(creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
    }

    @Test
    void countsEveryQualifyingCreatureForRollBonus() {
        Card first = putCreatureIntoGraveyard();
        Card second = putCreatureIntoGraveyard();
        setRoll(13);

        castRevivify();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .doesNotContain(first.getId(), second.getId());
    }

    @Test
    void untrackedCreaturesAndNoncreatureCardsDoNotIncreaseBonusOrReturn() {
        Card olderCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(olderCreature));
        Card qualifying = putCreatureIntoGraveyard();
        setRoll(13);

        castRevivify();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(qualifying.getId()).doesNotContain(olderCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(olderCreature.getId());
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Revivify");
    }

    @Test
    void highRollLeavesUntrackedCreatureInGraveyard() {
        Card olderCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(olderCreature));
        Card qualifying = putCreatureIntoGraveyard();
        setRoll(20);

        castRevivify();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(qualifying.getId()).doesNotContain(olderCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(olderCreature.getId());
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void returnedCreaturesSeeEachOtherEnterSimultaneously() {
        Card first = putCreatureIntoGraveyard(new SoulWarden());
        Card second = putCreatureIntoGraveyard(new SoulWarden());
        harness.setLife(player1, 20);
        setRoll(20);

        castRevivify();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId());
        harness.assertLife(player1, 22);
    }

    @Test
    void opponentsDeadCreatureDoesNotIncreaseBonusOrReturn() {
        Card opponentCreature = new GrizzlyBears();
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, opponentCreature);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, opponentPermanent.getId());
        Card ownCreature = putCreatureIntoGraveyard();
        setRoll(13);

        castRevivify();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(ownCreature.getId()).doesNotContain(opponentCreature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .contains(opponentCreature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void resolvesWithNoQualifyingCreatures() {
        setRoll(20);

        castRevivify();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Revivify");
        assertThat(gd.stack).isEmpty();
    }
    private Card putCreatureIntoGraveyard() {
        return putCreatureIntoGraveyard(new GrizzlyBears());
    }

    private Card putCreatureIntoGraveyard(Card creature) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, creature);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, permanent.getId());
        return creature;
    }

    private void castRevivify() {
        harness.castFromHand(player1, new Revivify(), "{2}{W}");
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
