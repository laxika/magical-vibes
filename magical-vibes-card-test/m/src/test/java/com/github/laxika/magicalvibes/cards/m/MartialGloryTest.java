package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.a.ArmoredTransport;
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

@CardUsed({MartialGlory.class, ArmoredTransport.class})
class MartialGloryTest extends BaseCardTest {

    @Test
    @DisplayName("First target gets +3/+0 and second target gets +0/+3")
    void boostsBothTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ArmoredTransport());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        harness.setHand(player1, List.of(new MartialGlory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID firstId = first.getId();
        UUID secondId = second.getId();
        harness.castInstant(player1, 0, List.of(firstId, secondId));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isEqualTo(0);

        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("Both targets may be the same creature, which then gets +3/+3")
    void sameCreatureForBothTargets() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new ArmoredTransport());
        harness.setHand(player1, List.of(new MartialGlory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearId = bear.getId();
        harness.castInstant(player1, 0, List.of(bearId, bearId));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boosts wear off at end of turn")
    void boostsWearOffAtEndOfTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ArmoredTransport());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        harness.setHand(player1, List.of(new MartialGlory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID firstId = first.getId();
        UUID secondId = second.getId();
        harness.castInstant(player1, 0, List.of(firstId, secondId));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isZero();

        assertThat(PermanentGrants.powerBoostOf(gd, second)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isZero();
    }

    @Test
    @DisplayName("Second target still gets +0/+3 when first target is removed before resolution")
    void secondBoostAppliesWhenFirstTargetRemoved() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ArmoredTransport());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        harness.setHand(player1, List.of(new MartialGlory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID firstId = first.getId();
        UUID secondId = second.getId();
        harness.castInstant(player1, 0, List.of(firstId, secondId));

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("First target still gets only +3/+0 when the second target leaves")
    void firstBoostAppliesWhenSecondTargetRemoved() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ArmoredTransport());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        harness.setHand(player1, List.of(new MartialGlory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isZero();
        harness.assertInGraveyard(player1, "Martial Glory");
    }

    @Test
    @DisplayName("Spell does not resolve when its shared target leaves")
    void sharedTargetRemovedBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArmoredTransport());
        harness.setHand(player1, List.of(new MartialGlory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, List.of(target.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Martial Glory");
    }

    @Test
    @DisplayName("Both creature target occurrences are required when casting")
    void cannotCastWithOnlyOneTargetOccurrence() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArmoredTransport());
        harness.setHand(player1, List.of(new MartialGlory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Martial Glory");
    }
}
