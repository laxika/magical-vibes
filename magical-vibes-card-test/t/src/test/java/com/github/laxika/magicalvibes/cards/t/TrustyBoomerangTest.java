package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OtterPenguin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrustyBoomerang.class, OtterPenguin.class, Forest.class})
class TrustyBoomerangTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip attaches Trusty Boomerang to target creature")
    void equipAttachesToCreature() {
        Permanent creature = addReady(player1, new OtterPenguin());
        Permanent boomerang = addReady(player1, new TrustyBoomerang());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(boomerang.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature taps a target creature and returns Trusty Boomerang to its owner's hand")
    void tapsCreatureAndReturnsBoomerangToHand() {
        Permanent creature = addReady(player1, new OtterPenguin());
        Permanent boomerang = addReady(player1, new TrustyBoomerang());
        boomerang.setAttachedTo(creature.getId());
        Permanent target = addReady(player2, new OtterPenguin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(boomerang.getAttachedTo()).isNull();
        harness.assertInHand(player1, "Trusty Boomerang");
        harness.assertNotOnBattlefield(player1, "Trusty Boomerang");
    }

    @Test
    @DisplayName("Granted ability cannot target a land")
    void cannotTargetLand() {
        Permanent creature = addReady(player1, new OtterPenguin());
        Permanent boomerang = addReady(player1, new TrustyBoomerang());
        boomerang.setAttachedTo(creature.getId());
        Permanent target = addReady(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void illegalTargetPreventsBoomerangReturning() {
        Permanent creature = addReady(player1, new OtterPenguin());
        Permanent boomerang = addReady(player1, new TrustyBoomerang());
        boomerang.setAttachedTo(creature.getId());
        Permanent target = addReady(player2, new OtterPenguin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(boomerang.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertOnBattlefield(player1, "Trusty Boomerang");
        harness.assertNotInHand(player1, "Trusty Boomerang");
    }

    @Test
    void alreadyTappedTargetStillReturnsBoomerang() {
        Permanent creature = addReady(player1, new OtterPenguin());
        Permanent boomerang = addReady(player1, new TrustyBoomerang());
        boomerang.setAttachedTo(creature.getId());
        Permanent target = addReady(player2, new OtterPenguin());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.assertInHand(player1, "Trusty Boomerang");
        harness.assertNotOnBattlefield(player1, "Trusty Boomerang");
    }

    @Test
    void canTargetTheEquippedCreatureItself() {
        Permanent creature = addReady(player1, new OtterPenguin());
        Permanent boomerang = addReady(player1, new TrustyBoomerang());
        boomerang.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.assertInHand(player1, "Trusty Boomerang");
    }

    @Test
    void returnsOnlyTheBoomerangThatGrantedTheActivatedAbility() {
        Permanent creature = addReady(player1, new OtterPenguin());
        Permanent first = addReady(player1, new TrustyBoomerang());
        Permanent second = addReady(player1, new TrustyBoomerang());
        first.setAttachedTo(creature.getId());
        second.setAttachedTo(creature.getId());
        Permanent target = addReady(player2, new OtterPenguin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(first.getCard()).doesNotContain(second.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(second).doesNotContain(first);
        assertThat(second.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void stillReturnsBoomerangAfterAttachmentChanges() {
        Permanent creature = addReady(player1, new OtterPenguin());
        Permanent boomerang = addReady(player1, new TrustyBoomerang());
        Permanent otherCreature = addReady(player1, new OtterPenguin());
        boomerang.setAttachedTo(creature.getId());
        Permanent target = addReady(player2, new OtterPenguin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        boomerang.setAttachedTo(otherCreature.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(otherCreature.isTapped()).isFalse();
        harness.assertInHand(player1, "Trusty Boomerang");
        harness.assertNotOnBattlefield(player1, "Trusty Boomerang");
    }

    @Test
    void returnsBoomerangToOwnerRatherThanAbilityController() {
        Permanent creature = addReady(player1, new OtterPenguin());
        Permanent boomerang = addReady(player2, new TrustyBoomerang());
        boomerang.setAttachedTo(creature.getId());
        Permanent target = addReady(player2, new OtterPenguin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.assertInHand(player2, "Trusty Boomerang");
        harness.assertNotInHand(player1, "Trusty Boomerang");
        harness.assertNotOnBattlefield(player2, "Trusty Boomerang");
    }

    @Test
    void summoningSickCreatureCannotActivateGrantedTapAbility() {
        Permanent creature = addReady(player1, new OtterPenguin());
        creature.setSummoningSick(true);
        Permanent boomerang = addReady(player1, new TrustyBoomerang());
        boomerang.setAttachedTo(creature.getId());
        Permanent target = addReady(player2, new OtterPenguin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        harness.assertNotInHand(player1, "Trusty Boomerang");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
