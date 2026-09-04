package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HallowedGround.class, AdarkarWastes.class, BalduvianBears.class, Plains.class})
class HallowedGroundTest extends BaseCardTest {

    @Test
    @DisplayName("Ability returns target nonsnow land you control to its owner's hand")
    void bouncesControlledNonsnowLand() {
        Permanent hallowedGround = addHallowedGround(player1);
        Permanent plains = addLand(player1, false);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, plains.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Plains");
        assertCardInHand(player1, plains);
        assertThat(hallowedGround.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability returns a nonbasic nonsnow land you control")
    void bouncesControlledNonbasicLand() {
        addHallowedGround(player1);
        Permanent wastes = harness.addToBattlefieldAndReturn(player1, new AdarkarWastes());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, wastes.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Adarkar Wastes");
        assertCardInHand(player1, wastes);
    }

    @Test
    @DisplayName("Ability requires two white mana")
    void requiresTwoWhiteMana() {
        addHallowedGround(player1);
        Permanent plains = addLand(player1, false);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns a controlled land to its owner's hand")
    void returnsControlledLandToItsOwner() {
        addHallowedGround(player1);
        Permanent plains = addLand(player1, false);
        gd.stolenCreatures.put(plains.getId(), player2.getId());
        gd.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(),
                "Test control effect",
                null,
                player1.getId(),
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                plains.getId(),
                null,
                null,
                EffectDuration.PERMANENT,
                0));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, plains.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Plains");
        assertCardInHand(player2, plains);
        assertCardNotInHand(player1, plains);
    }

    @Test
    @DisplayName("Cannot target a snow land")
    void cannotTargetSnowLand() {
        addHallowedGround(player1);
        Permanent snowLand = addLand(player1, true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, snowLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's land")
    void cannotTargetOpponentsLand() {
        addHallowedGround(player1);
        Permanent plains = addLand(player2, false);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        addHallowedGround(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not return a land that becomes snow before resolution")
    void targetBecomingSnowBeforeResolutionIsIllegal() {
        addHallowedGround(player1);
        Permanent plains = addLand(player1, false);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, plains.getId());
        TestCards.mutableCard(plains).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Plains");
        assertCardNotInHand(player1, plains);
    }

    private Permanent addHallowedGround(Player player) {
        return harness.addToBattlefieldAndReturn(player, new HallowedGround());
    }

    private Permanent addLand(Player player, boolean snow) {
        Permanent land = harness.addToBattlefieldAndReturn(player, new Plains());
        if (snow) {
            TestCards.mutableCard(land).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        }
        return land;
    }

    private void assertCardInHand(Player player, Permanent permanent) {
        assertThat(gd.playerHands.get(player.getId()))
                .anyMatch(card -> card.getId().equals(permanent.getCard().getId()));
    }

    private void assertCardNotInHand(Player player, Permanent permanent) {
        assertThat(gd.playerHands.get(player.getId()))
                .noneMatch(card -> card.getId().equals(permanent.getCard().getId()));
    }
}
