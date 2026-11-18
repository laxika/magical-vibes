package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SatyrRambler;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoordinatedAssault.class, SatyrRambler.class, Mountain.class, LightningStrike.class})
class CoordinatedAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Gives up to two target creatures +1/+0 and first strike")
    void boostsTwoTargetsAndGrantsFirstStrike() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        cast(List.of(first.getId(), second.getId()));

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(1);
        assertThat(PermanentGrants.keywordsOf(gd, first)).contains(Keyword.FIRST_STRIKE);
        assertThat(PermanentGrants.keywordsOf(gd, second)).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Allows only one target")
    void allowsSingleTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        cast(List.of(target.getId()));

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(1);
        assertThat(PermanentGrants.keywordsOf(gd, target)).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Boost and first strike wear off at cleanup")
    void wearsOffAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        cast(List.of(target.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isZero();
        assertThat(PermanentGrants.keywordsOf(gd, target)).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new CoordinatedAssault()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void allowsZeroTargetsWithoutAffectingCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SatyrRambler());
        cast(List.of());

        assertThat(PermanentGrants.powerBoostOf(gd, creature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, creature)).isZero();
        assertThat(PermanentGrants.keywordsOf(gd, creature)).doesNotContain(Keyword.FIRST_STRIKE);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Coordinated Assault");
    }

    @Test
    void canTargetCreaturesControlledByDifferentPlayers() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new SatyrRambler());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        cast(List.of(own.getId(), opposing.getId()));

        assertThat(PermanentGrants.powerBoostOf(gd, own)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, opposing)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, own)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opposing)).isZero();
        assertThat(PermanentGrants.keywordsOf(gd, own)).contains(Keyword.FIRST_STRIKE);
        assertThat(PermanentGrants.keywordsOf(gd, opposing)).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    void cannotChooseThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        harness.setHand(player1, List.of(new CoordinatedAssault()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseTheSameCreatureTwice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        harness.setHand(player1, List.of(new CoordinatedAssault()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stillAffectsRemainingTargetWhenFirstTargetDiesInResponse() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        harness.setHand(player1, List.of(new CoordinatedAssault(), new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.castAndResolveInstant(player1, 0, first.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first).contains(second);
        assertThat(PermanentGrants.powerBoostOf(gd, first)).isZero();
        assertThat(PermanentGrants.keywordsOf(gd, first)).doesNotContain(Keyword.FIRST_STRIKE);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isZero();
        assertThat(PermanentGrants.keywordsOf(gd, second)).contains(Keyword.FIRST_STRIKE);
        harness.assertInGraveyard(player1, "Coordinated Assault");
    }

    @Test
    void doesNotAffectOtherCreaturesWhenOnlyTargetDiesInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player1, new SatyrRambler());
        harness.setHand(player1, List.of(new CoordinatedAssault(), new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(PermanentGrants.powerBoostOf(gd, untargeted)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, untargeted)).isZero();
        assertThat(PermanentGrants.keywordsOf(gd, untargeted)).doesNotContain(Keyword.FIRST_STRIKE);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Coordinated Assault");
    }

    private void cast(List<UUID> targets) {
        harness.setHand(player1, List.of(new CoordinatedAssault()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, targets);
    }
}
