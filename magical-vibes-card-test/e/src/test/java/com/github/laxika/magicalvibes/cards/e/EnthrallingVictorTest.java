package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FieryImpulse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MaritimeGuard;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnthrallingVictor.class, GrizzlyBears.class, HillGiant.class,
        MaritimeGuard.class, TitanicGrowth.class, FieryImpulse.class})
class EnthrallingVictorTest extends BaseCardTest {

    @Test
    @DisplayName("Steals, untaps and hastes an opponent's creature with power 2 or less")
    void stealsUntapsAndHastes() {
        Permanent target = addCreature(new GrizzlyBears(), player2);
        target.tap();

        castVictor(target.getId());
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Control and haste wear off at end of turn")
    void controlExpiresAtCleanup() {
        Permanent target = addCreature(new GrizzlyBears(), player2);
        castVictor(target.getId());
        harness.passBothPriorities(); // resolve ETB trigger

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature with power 3 or more")
    void rejectsHighPowerTarget() {
        Permanent giant = addCreature(new HillGiant(), player2);

        assertThatThrownBy(() -> castVictor(giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void rejectsOwnCreature() {
        Permanent own = addCreature(new GrizzlyBears(), player1);

        assertThatThrownBy(() -> castVictor(own.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Entire trigger fails if the target's power grows above two before resolution")
    void targetBecomesTooPowerfulBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MaritimeGuard());
        target.tap();
        castVictor(target.getId());

        harness.setHand(player2, List.of(new TitanicGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Trigger still steals, untaps and grants haste after Victor dies")
    void triggerResolvesAfterSourceDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MaritimeGuard());
        target.tap();
        castVictor(target.getId());
        UUID victorId = harness.getPermanentId(player1, "Enthralling Victor");

        harness.setHand(player2, List.of(new FieryImpulse()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, victorId);
        harness.assertInGraveyard(player1, "Enthralling Victor");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Victor can enter when there are no legal targets")
    void entersWithoutLegalTargets() {
        castVictor(null);

        harness.assertOnBattlefield(player1, "Enthralling Victor");
        assertThat(gd.stack).isEmpty();
    }
    private Permanent addCreature(Card card, Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }

    private void castVictor(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EnthrallingVictor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell
    }
}
