package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.ManaforgeCinder;
import com.github.laxika.magicalvibes.cards.w.WallOfVines;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TraitorsRoar.class, HillGiant.class, WallOfVines.class, GrizzlyBears.class, ManaforgeCinder.class})
class TraitorsRoarTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    @Test
    @DisplayName("Taps the target and it deals damage equal to its power to its controller")
    void tapsAndDealsPowerDamage() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new TraitorsRoar()));
        addMana();

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.castAndResolveSorcery(player1, 0, List.of(targetId));

        // Hill Giant is 3/3 -> its controller loses 3, and the creature ends up tapped and unharmed.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(targetId))
                .allMatch(Permanent::isTapped)
                .allMatch(p -> p.getMarkedDamage() == 0);
    }

    @Test
    @DisplayName("A 0-power creature is tapped but deals no damage")
    void zeroPowerDealsNoDamage() {
        harness.addToBattlefield(player2, new WallOfVines());
        harness.setHand(player1, List.of(new TraitorsRoar()));
        addMana();

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Wall of Vines");
        harness.castAndResolveSorcery(player1, 0, List.of(targetId));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(targetId))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Cannot target an already-tapped creature")
    void cannotTargetTappedCreature() {
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tapped.tap();
        UUID tappedId = tapped.getId();
        harness.setHand(player1, List.of(new TraitorsRoar()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(tappedId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tappedBeforeResolutionDoesNotDealDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ManaforgeCinder());
        harness.setHand(player1, List.of(new TraitorsRoar()));
        addMana();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player1, 0, List.of(target.getId()));
        target.tap();
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore);
        assertThat(target.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Traitor's Roar");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ManaforgeCinder());
        harness.setHand(player1, List.of(new TraitorsRoar()));
        addMana();
        int casterLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));

        assertThat(target.isTapped()).isTrue();
        harness.assertLife(player1, casterLife - 1);
        harness.assertLife(player2, opponentLife);
    }

    @Test
    void usesPowerAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ManaforgeCinder());
        harness.setHand(player1, List.of(new TraitorsRoar()));
        addMana();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player1, 0, List.of(target.getId()));
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 3);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void conspireWithSameTargetDealsDamageOnlyOnce() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ManaforgeCinder());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ManaforgeCinder());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ManaforgeCinder());
        harness.setHand(player1, List.of(new TraitorsRoar()));
        addMana();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castWithConspire(player1, 0, target.getId(), List.of(first.getId(), second.getId()));
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, lifeBefore - 1);
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Traitor's Roar")).hasSize(1);
    }

    @Test
    void conspireCanChooseNewTargetForCopy() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ManaforgeCinder());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ManaforgeCinder());
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new ManaforgeCinder());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new ManaforgeCinder());
        harness.setHand(player1, List.of(new TraitorsRoar()));
        addMana();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castWithConspire(player1, 0, originalTarget.getId(), List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        harness.assertLife(player2, lifeBefore - 2);
        assertThat(originalTarget.isTapped()).isTrue();
        assertThat(copyTarget.isTapped()).isTrue();
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Traitor's Roar")).hasSize(1);
    }

    @Test
    void conspireRejectsCreaturesThatDoNotShareSpellColor() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ManaforgeCinder());
        harness.setHand(player1, List.of(new TraitorsRoar()));
        addMana();

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, target.getId(),
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
