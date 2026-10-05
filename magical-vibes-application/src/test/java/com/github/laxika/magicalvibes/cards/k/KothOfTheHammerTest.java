package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KothOfTheHammer.class, Mountain.class, Forest.class})
class KothOfTheHammerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with 3 loyalty")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new KothOfTheHammer()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Koth of the Hammer"));
        Permanent koth = bf.stream().filter(p -> p.getCard().getName().equals("Koth of the Hammer")).findFirst().orElseThrow();
        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("+1 untaps target Mountain and makes it a 4/4 red Elemental creature")
    void plusOneUntapsAndAnimatesMountain() {
        Permanent koth = addReadyKoth(player1);
        Permanent mountain = addMountain(player1);
        mountain.tap();

        harness.activateAbility(player1, 0, 0, null, mountain.getId());
        harness.passBothPriorities();

        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(mountain.isTapped()).isFalse();
        assertThat(mountain.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(mountain.getAnimatedPower()).isEqualTo(4);
        assertThat(mountain.getAnimatedToughness()).isEqualTo(4);
        assertThat(mountain.getTransientSubtypes()).contains(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("+1 can target an untapped Mountain (still animates it)")
    void plusOneCanTargetUntappedMountain() {
        addReadyKoth(player1);
        Permanent mountain = addMountain(player1);

        harness.activateAbility(player1, 0, 0, null, mountain.getId());
        harness.passBothPriorities();

        assertThat(mountain.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(mountain.getAnimatedPower()).isEqualTo(4);
    }

    @Test
    @DisplayName("+1 cannot target a non-Mountain land")
    void plusOneCannotTargetNonMountain() {
        addReadyKoth(player1);
        // Add a Forest (not a Mountain)
        Permanent forestPerm = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forestPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Mountain");
    }

    @Test
    @DisplayName("+1 can target opponent's Mountain")
    void plusOneCanTargetOpponentsMountain() {
        addReadyKoth(player1);
        Permanent opponentMountain = addMountain(player2);
        opponentMountain.tap();

        harness.activateAbility(player1, 0, 0, null, opponentMountain.getId());
        harness.passBothPriorities();

        assertThat(opponentMountain.isTapped()).isFalse();
        assertThat(opponentMountain.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(opponentMountain.getAnimatedPower()).isEqualTo(4);
    }

    @Test
    @DisplayName("-2 adds red mana for each Mountain controlled")
    void minusTwoAddsManaPerMountain() {
        Permanent koth = addReadyKoth(player1);
        addMountain(player1);
        addMountain(player1);
        addMountain(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("-2 adds zero mana when controlling no Mountains")
    void minusTwoAddsZeroManaWithNoMountains() {
        addReadyKoth(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("-2 does not count opponent's Mountains")
    void minusTwoDoesNotCountOpponentMountains() {
        addReadyKoth(player1);
        addMountain(player1);
        addMountain(player2);
        addMountain(player2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("-5 creates an emblem")
    void minusFiveCreatesEmblem() {
        Permanent koth = addReadyKoth(player1);
        koth.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Emblem lets a Mountain tap to deal damage after Koth dies")
    void emblemLetsMountainDealDamage() {
        Permanent koth = addReadyKoth(player1);
        koth.setCounterCount(CounterType.LOYALTY, 5);
        Permanent mountain = addMountain(player1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Koth of the Hammer");
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        assertThat(mountain.isTapped()).isTrue();
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Emblem persists after Koth dies")
    void emblemPersistsAfterKothDies() {
        Permanent koth = addReadyKoth(player1);
        koth.setCounterCount(CounterType.LOYALTY, 5);

        // Use -5, Koth goes to 0 and dies
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Koth should be gone
        harness.assertNotOnBattlefield(player1, "Koth of the Hammer");
        // But emblem persists
        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot activate -5 with only 3 loyalty")
    void cannotActivateUltimateWithInsufficientLoyalty() {
        addReadyKoth(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Koth survives when -2 brings loyalty to 1")
    void minusTwoFromThreeLoyaltySurvives() {
        Permanent koth = addReadyKoth(player1);
        addMountain(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        // 3 - 2 = 1, Koth should survive
        harness.assertOnBattlefield(player1, "Koth of the Hammer");
        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void animationRetainsLandTypeAndManaAbilityAndExpires() {
        addReadyKoth(player1);
        Permanent mountain = addMountain(player1);
        mountain.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, mountain.getId());
        harness.passBothPriorities();

        var query = harness.getGameQueryService();
        GameData gd = harness.getGameData();
        assertThat(query.isCreature(gd, mountain)).isTrue();
        assertThat(query.isLand(gd, mountain)).isTrue();
        assertThat(query.getEffectivePower(gd, mountain)).isEqualTo(4);
        assertThat(query.getEffectiveToughness(gd, mountain)).isEqualTo(4);
        assertThat(query.getEffectiveColors(gd, mountain)).containsExactly(CardColor.RED);
        harness.tapPermanent(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(query.isCreature(gd, mountain)).isFalse();
        assertThat(query.isLand(gd, mountain)).isTrue();
        assertThat(query.getEffectiveColors(gd, mountain)).isEmpty();
        assertThat(mountain.getTransientSubtypes()).doesNotContain(CardSubtype.ELEMENTAL);
    }

    @Test
    void animatedNewMountainCannotTapForMana() {
        addReadyKoth(player1);
        Permanent mountain = addMountain(player1);
        mountain.setSummoningSick(true);
        harness.activateAbility(player1, 0, 0, null, mountain.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.tapPermanent(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mountain.isTapped()).isFalse();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void cannotActivateAnotherLoyaltyAbilityInSameTurn() {
        addReadyKoth(player1);
        Permanent mountain = addMountain(player1);
        harness.activateAbility(player1, 0, 0, null, mountain.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusTwoUsesStackAndCountsMountainsAtResolution() {
        addReadyKoth(player1);
        addMountain(player1);
        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        addMountain(player1);
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void emblemAppliesToMountainsEnteringLaterButNotForestsOrOpponentsMountains() {
        Permanent koth = addReadyKoth(player1);
        koth.setCounterCount(CounterType.LOYALTY, 5);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent mountain = addMountain(player1);
        harness.addToBattlefield(player1, new Forest());
        addMountain(player2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(mountain.isTapped()).isTrue();
        harness.assertLife(player2, 19);

        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void emblemDamageCanTargetPlaneswalkers() {
        Permanent target = addReadyKoth(player2);
        Permanent koth = addReadyKoth(player1);
        koth.setCounterCount(CounterType.LOYALTY, 5);
        addMountain(player1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void emblemDamageCanTargetAnimatedMountains() {
        addReadyKoth(player2);
        Permanent target = addMountain(player2);
        harness.activateAbility(player2, 0, 0, null, target.getId());
        harness.passBothPriorities();

        Permanent koth = addReadyKoth(player1);
        koth.setCounterCount(CounterType.LOYALTY, 5);
        addMountain(player1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Mountain");
    }

    private Permanent addReadyKoth(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new KothOfTheHammer());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addMountain(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Mountain());
    }
}
