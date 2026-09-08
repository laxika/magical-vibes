package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SandGolem;
import com.github.laxika.magicalvibes.cards.s.StalkingTiger;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CatacombDragon.class, ZhalfirinKnight.class, StalkingTiger.class,
        CrashOfRhinos.class, SandGolem.class})
class CatacombDragonTest extends BaseCardTest {

    @Test
    @DisplayName("A 2/2 blocker gets -1/-0 (half its power, rounded down)")
    void shrinksBlockerByHalfItsPower() {
        addAttackingDragon();
        Permanent blocker = addFlyingBlocker(new ZhalfirinKnight());

        block();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
    }

    @Test
    @DisplayName("An odd power is rounded down: a 3/3 blocker gets -1/-0")
    void roundsDown() {
        addAttackingDragon();
        Permanent blocker = addFlyingBlocker(new StalkingTiger());

        block();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
    }

    @Test
    @DisplayName("An 8/4 blocker gets -4/-0")
    void shrinksLargeBlocker() {
        addAttackingDragon();
        Permanent blocker = addFlyingBlocker(new CrashOfRhinos());

        block();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(4);
    }

    @Test
    @DisplayName("An artifact creature blocker is unaffected")
    void artifactBlockerUnaffected() {
        addAttackingDragon();
        Permanent blocker = addFlyingBlocker(new SandGolem());

        block();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(3);
    }

    @Test
    @DisplayName("A Dragon blocker is unaffected")
    void dragonBlockerUnaffected() {
        addAttackingDragon();
        Permanent blocker = addFlyingBlocker(new StalkingTiger());
        blocker.getGrantedSubtypes().add(CardSubtype.DRAGON);

        block();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(3);
    }

    @Test
    @DisplayName("A hexproof blocker is still affected because the trigger does not target")
    void affectsHexproofBlocker() {
        addAttackingDragon();
        Permanent blocker = addFlyingBlocker(new ZhalfirinKnight());
        blocker.getGrantedKeywords().add(Keyword.HEXPROOF);

        block();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(1);
    }

    @Test
    @DisplayName("The reduction uses the blocker's power when the trigger resolves")
    void usesBlockersPowerAtResolution() {
        addAttackingDragon();
        Permanent blocker = addFlyingBlocker(new StalkingTiger());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        blocker.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
    }

    @Test
    @DisplayName("With two blockers only the qualifying one shrinks")
    void onlyQualifyingBlockerShrinks() {
        addAttackingDragon();
        Permanent qualifyingBlocker = addFlyingBlocker(new ZhalfirinKnight());
        Permanent dragonBlocker = addFlyingBlocker(new StalkingTiger());
        dragonBlocker.getGrantedSubtypes().add(CardSubtype.DRAGON);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, qualifyingBlocker)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, dragonBlocker)).isEqualTo(3);
    }

    @Test
    @DisplayName("The reduction expires at end of turn")
    void reductionExpiresAtEndOfTurn() {
        addAttackingDragon();
        Permanent blocker = addFlyingBlocker(new ZhalfirinKnight());

        block();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
    }

    private void addAttackingDragon() {
        Permanent dragon = addCreatureReady(player1, new CatacombDragon());
        dragon.setAttacking(true);
    }

    private Permanent addFlyingBlocker(Card card) {
        Permanent permanent = addCreatureReady(player2, card);
        permanent.getGrantedKeywords().add(Keyword.FLYING);
        return permanent;
    }

    private void block() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
    }
}
