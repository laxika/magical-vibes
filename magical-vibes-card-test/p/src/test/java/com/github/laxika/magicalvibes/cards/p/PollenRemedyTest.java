package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AncientSpider;
import com.github.laxika.magicalvibes.cards.m.MeteorCrater;
import com.github.laxika.magicalvibes.cards.m.MagmaBurst;
import com.github.laxika.magicalvibes.cards.s.StandardBearer;
import com.github.laxika.magicalvibes.cards.v.VoiceOfAll;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PollenRemedy.class, AncientSpider.class, MeteorCrater.class, MagmaBurst.class,
        VoiceOfAll.class, StandardBearer.class})
class PollenRemedyTest extends BaseCardTest {

    @Test
    void preventsThreeDamageDividedAmongCreatureAndPlayer() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, Map.of(spider.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(spider.getDamagePreventionShield()).isEqualTo(2);
        assertThat(gameData.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    void kickedPreventsSixDamageAndSacrificesALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MeteorCrater());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, null, Map.of(spider.getId(), 3, player2.getId(), 3),
                List.of(), List.of(), false, land.getId(), null, null, null, null, true);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(spider.getDamagePreventionShield()).isEqualTo(3);
        assertThat(gameData.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(land.getId()));
        harness.assertInGraveyard(player1, "Meteor Crater");
    }

    @Test
    void preventionAssignmentsMustSumToThree() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(spider.getId(), 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Prevention assignments must sum to 3");
    }

    @Test
    void kickerRequiresSacrificingALand() {
        Permanent nonland = harness.addToBattlefieldAndReturn(player1, new AncientSpider());
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null,
                Map.of(player2.getId(), 6), List.of(), List.of(), false, nonland.getId(), null,
                null, null, null, true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayChooseZeroTargets() {
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, Map.<UUID, Integer>of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pollen Remedy");
        assertThat(gd.playerDamagePreventionShields).isEmpty();
    }

    @Test
    void cannotTargetCreatureWithProtectionFromWhite() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new VoiceOfAll());
        angel.setChosenColor(CardColor.WHITE);
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(angel.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
    }

    @Test
    void remainingTargetKeepsItsOriginalShareWhenAnotherTargetLeaves() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, Map.of(spider.getId(), 2, player2.getId(), 1));
        gd.playerBattlefields.get(player2.getId()).remove(spider);
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(spider.getDamagePreventionShield()).isZero();
        harness.assertInGraveyard(player1, "Pollen Remedy");
    }

    @Test
    void creatureShieldPreventsOnlyItsAssignedDamage() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new PollenRemedy(), new MagmaBurst()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, Map.of(spider.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0, spider.getId());

        assertThat(spider.getMarkedDamage()).isEqualTo(1);
        assertThat(spider.getDamagePreventionShield()).isZero();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    void playerShieldIsConsumedAcrossSuccessiveDamageEvents() {
        harness.setHand(player1, List.of(new PollenRemedy(), new MagmaBurst(), new MagmaBurst()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, Map.of(player2.getId(), 3));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 20);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 17);
    }

    @Test
    void unusedPreventionExpiresAtEndOfTurn() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, Map.of(spider.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(spider.getDamagePreventionShield()).isZero();
        assertThat(gd.playerDamagePreventionShields).isEmpty();
    }

    @Test
    void eachChosenTargetMustReceiveAtLeastOnePrevention() {
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                Map.of(player1.getId(), 0, player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Each prevention assignment must be positive");
    }

    @Test
    void cannotAssignPreventionToANoncreatureLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MeteorCrater());
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(land.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedAssignmentsMustSumToSix() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MeteorCrater());
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null,
                Map.of(player2.getId(), 3), List.of(), List.of(), false, land.getId(), null,
                null, null, null, true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Prevention assignments must sum to 6");
    }

    @Test
    void cannotSacrificeAnOpponentsLandForKicker() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new MeteorCrater());
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null,
                Map.of(player2.getId(), 6), List.of(), List.of(), false, land.getId(), null,
                null, null, null, true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mustIncludeAnOpponentsFlagbearerWhenChoosingPreventionTargets() {
        harness.addToBattlefield(player2, new StandardBearer());
        harness.setHand(player1, List.of(new PollenRemedy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(player1.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }
}
