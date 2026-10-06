package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkywaySniper.class, SuntailHawk.class, GrizzlyBears.class})
class SkywaySniperTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target creature with flying")
    void dealsDamageToFlyingCreature() {
        harness.addToBattlefield(player1, new SkywaySniper());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Suntail Hawk");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        harness.addToBattlefield(player1, new SkywaySniper());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target its controller's flying creature")
    void canTargetOwnFlyingCreature() {
        harness.addToBattlefield(player1, new SkywaySniper());
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, hawk.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Suntail Hawk");
    }

    @Test
    @DisplayName("Can activate repeatedly while tapped and summoning sick")
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        Permanent sniper = harness.addToBattlefieldAndReturn(player1, new SkywaySniper());
        sniper.setTapped(true);
        Permanent firstHawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent secondHawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, firstHawk.getId());
        harness.activateAbility(player1, 0, null, secondHawk.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(sniper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Requires green mana")
    void cannotPayWithOnlyColorlessMana() {
        harness.addToBattlefield(player1, new SkywaySniper());
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, hawk.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Requires three mana in total")
    void cannotActivateWithInsufficientMana() {
        harness.addToBattlefield(player1, new SkywaySniper());
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, hawk.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Does not damage a target that loses flying before resolution")
    void targetMustStillHaveFlyingOnResolution() {
        harness.addToBattlefield(player1, new SkywaySniper());
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, hawk.getId());
        hawk.getRemovedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Suntail Hawk");
        assertThat(hawk.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolves after Skyway Sniper leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent sniper = harness.addToBattlefieldAndReturn(player1, new SkywaySniper());
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, hawk.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, sniper));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Skyway Sniper");
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }
}
