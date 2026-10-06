package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.cards.w.WelkinTern;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MerfolkSeastalkers.class, KrakenHatchling.class, WelkinTern.class, Island.class})
class MerfolkSeastalkersTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability taps target creature without flying")
    void resolvingTapsNonFlyingCreature() {
        addCreatureReady(player1, new MerfolkSeastalkers());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent target = addCreatureReady(player2, new KrakenHatchling());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetFlyingCreature() {
        addCreatureReady(player1, new MerfolkSeastalkers());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent flyer = addCreatureReady(player2, new WelkinTern());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetCreatureWithGrantedFlying() {
        addCreatureReady(player1, new MerfolkSeastalkers());
        Permanent target = addCreatureReady(player2, new KrakenHatchling());
        target.getGrantedKeywords().add(Keyword.FLYING);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotTapTargetThatGainsFlyingBeforeResolution() {
        addCreatureReady(player1, new MerfolkSeastalkers());
        Permanent target = addCreatureReady(player2, new KrakenHatchling());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        target.getGrantedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MerfolkSeastalkers());
        source.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KrakenHatchling());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canActivateTwiceWithoutTappingSource() {
        Permanent source = addCreatureReady(player1, new MerfolkSeastalkers());
        Permanent first = addCreatureReady(player2, new KrakenHatchling());
        Permanent second = addCreatureReady(player2, new KrakenHatchling());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    void canTargetItself() {
        Permanent source = addCreatureReady(player1, new MerfolkSeastalkers());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
    }

    @Test
    void canTargetAlreadyTappedCreature() {
        addCreatureReady(player1, new MerfolkSeastalkers());
        Permanent target = addCreatureReady(player2, new KrakenHatchling());
        target.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new MerfolkSeastalkers());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotPayBlueCostWithOnlyColorlessMana() {
        addCreatureReady(player1, new MerfolkSeastalkers());
        Permanent target = addCreatureReady(player2, new KrakenHatchling());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityStillResolvesAfterSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new MerfolkSeastalkers());
        Permanent target = addCreatureReady(player2, new KrakenHatchling());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void abilityDoesNotTapTargetThatLeftBattlefield() {
        addCreatureReady(player1, new MerfolkSeastalkers());
        Permanent target = addCreatureReady(player2, new KrakenHatchling());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotBeBlockedWhenDefenderControlsIsland() {
        addCreatureReady(player1, new MerfolkSeastalkers());
        addCreatureReady(player2, new KrakenHatchling());
        harness.addToBattlefield(player2, new Island());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeBlockedWhenOnlyAttackerControlsIsland() {
        addCreatureReady(player1, new MerfolkSeastalkers());
        harness.addToBattlefield(player1, new Island());
        Permanent blocker = addCreatureReady(player2, new KrakenHatchling());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
