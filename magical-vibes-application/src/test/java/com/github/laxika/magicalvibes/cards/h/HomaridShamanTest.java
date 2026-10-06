package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.ElvishScout;
import com.github.laxika.magicalvibes.cards.f.FungalBloom;
import com.github.laxika.magicalvibes.cards.r.RiverMerfolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HomaridShaman.class, ElvishScout.class, RiverMerfolk.class, FungalBloom.class})
class HomaridShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target green creature")
    void tapsTargetGreenCreature() {
        harness.addToBattlefield(player1, new HomaridShaman());
        Permanent scout = harness.addToBattlefieldAndReturn(player2, new ElvishScout());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, scout.getId());
        harness.passBothPriorities();

        assertThat(scout.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability does not tap Homarid Shaman")
    void activatingAbilityDoesNotTapSource() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new HomaridShaman());
        Permanent scout = harness.addToBattlefieldAndReturn(player2, new ElvishScout());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, scout.getId());
        harness.passBothPriorities();

        assertThat(shaman.isTapped()).isFalse();
        assertThat(scout.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-green creature")
    void cannotTargetNonGreenCreature() {
        harness.addToBattlefield(player1, new HomaridShaman());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new RiverMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, merfolk.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(merfolk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a green noncreature permanent")
    void cannotTargetGreenNoncreaturePermanent() {
        harness.addToBattlefield(player1, new HomaridShaman());
        Permanent bloom = harness.addToBattlefieldAndReturn(player2, new FungalBloom());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bloom.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bloom.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new HomaridShaman());
        shaman.tap();
        shaman.setSummoningSick(true);
        Permanent scout = harness.addToBattlefieldAndReturn(player2, new ElvishScout());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, scout.getId());
        harness.passBothPriorities();

        assertThat(scout.isTapped()).isTrue();
        assertThat(shaman.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap a green creature controlled by the ability's controller")
    void canTapOwnGreenCreature() {
        harness.addToBattlefield(player1, new HomaridShaman());
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new ElvishScout());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, scout.getId());
        harness.passBothPriorities();

        assertThat(scout.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target an already tapped green creature")
    void canTargetAlreadyTappedCreature() {
        harness.addToBattlefield(player1, new HomaridShaman());
        Permanent scout = harness.addToBattlefieldAndReturn(player2, new ElvishScout());
        scout.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, scout.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(scout.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without blue mana")
    void cannotActivateWithoutBlueMana() {
        harness.addToBattlefield(player1, new HomaridShaman());
        Permanent scout = harness.addToBattlefieldAndReturn(player2, new ElvishScout());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, scout.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(scout.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability resolves after Homarid Shaman leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new HomaridShaman());
        Permanent scout = harness.addToBattlefieldAndReturn(player2, new ElvishScout());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, scout.getId());
        gd.playerBattlefields.get(player1.getId()).remove(shaman);
        gd.playerGraveyards.get(player1.getId()).add(shaman.getCard());
        harness.passBothPriorities();

        assertThat(scout.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability does not tap a target that left the battlefield before resolution")
    void doesNotTapTargetThatLeftBattlefield() {
        harness.addToBattlefield(player1, new HomaridShaman());
        Permanent scout = harness.addToBattlefieldAndReturn(player2, new ElvishScout());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, scout.getId());
        gd.playerBattlefields.get(player2.getId()).remove(scout);
        gd.playerGraveyards.get(player2.getId()).add(scout.getCard());
        harness.passBothPriorities();

        assertThat(scout.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
