package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.t.TruePolymorph;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({DelinaWildMage.class, DireWolfProwler.class, TruePolymorph.class})
class DelinaWildMageTest extends BaseCardTest {

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
    void lowRollCreatesTappedAttackingNonlegendaryCopyAndExilesAtEndOfCombat() {
        setRolls(14);
        addCreatureReady(player1, new DelinaWildMage());
        Permanent wolf = addCreatureReady(player1, new DireWolfProwler());
        harness.addToBattlefield(player2, new DireWolfProwler());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, wolf.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        Permanent token = findPermanents(player1, "Dire Wolf Prowler").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(gqs.getEffectivePower(gd, wolf));
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(gqs.getEffectiveToughness(gd, wolf));
        finishCombatWithoutBlocks();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wolf);
    }

    @Test
    void highRollOffersAnotherRollAfterCreatingTheFirstCopy() {
        setRolls(15, 14);
        addCreatureReady(player1, new DelinaWildMage());
        Permanent wolf = addCreatureReady(player1, new DireWolfProwler());
        harness.addToBattlefield(player2, new DireWolfProwler());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, wolf.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(findPermanents(player1, "Dire Wolf Prowler"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    @Test
    void copyOfLegendaryCreatureIsNotLegendary() {
        setRolls(1);
        Permanent delina = addCreatureReady(player1, new DelinaWildMage());
        harness.addToBattlefield(player2, new DireWolfProwler());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, delina.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        Permanent token = findPermanents(player1, "Delina, Wild Mage").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    void highRollCanBeDeclinedAfterOneToken() {
        setRolls(20);
        addCreatureReady(player1, new DelinaWildMage());
        Permanent wolf = addCreatureReady(player1, new DireWolfProwler());
        harness.addToBattlefield(player2, new DireWolfProwler());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, wolf.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Dire Wolf Prowler"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void tokenDoesNotGainHaste() {
        Permanent token = createWolfToken();

        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
    }

    @Test
    void permanentCopyingDelinaTokenAlsoExilesAtEndOfCombat() {
        Permanent token = createWolfToken();
        Permanent otherWolf = addCreatureReady(player1, new DireWolfProwler());

        castTruePolymorph(otherWolf, token);
        finishCombatWithoutBlocks();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token, otherWolf);
    }

    @Test
    void tokenLosingExileAbilityBeforeEndOfCombatSurvives() {
        Permanent token = createWolfToken();
        Permanent original = findPermanents(player1, "Dire Wolf Prowler").stream()
                .filter(permanent -> !permanent.getCard().isToken())
                .findFirst().orElseThrow();

        castTruePolymorph(token, original);
        finishCombatWithoutBlocks();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
    }

    private Permanent createWolfToken() {
        setRolls(1);
        addCreatureReady(player1, new DelinaWildMage());
        Permanent wolf = addCreatureReady(player1, new DireWolfProwler());
        harness.addToBattlefield(player2, new DireWolfProwler());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, wolf.getId());
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, player2.getId());
        });
        return findPermanents(player1, "Dire Wolf Prowler").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
    }

    private void castTruePolymorph(Permanent target, Permanent copySource) {
        harness.setHand(player1, List.of(new TruePolymorph()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.castAndResolveInstant(player1, 0, List.of(target.getId(), copySource.getId())));
    }

    private void finishCombatWithoutBlocks() {
        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
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
