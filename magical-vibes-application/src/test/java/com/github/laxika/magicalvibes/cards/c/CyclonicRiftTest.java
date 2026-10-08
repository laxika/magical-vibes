package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RubblebackRhino;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CyclonicRift.class, DrudgeBeetle.class, Island.class, RubblebackRhino.class, ChromaticLantern.class})
class CyclonicRiftTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target nonland permanent you don't control to its owner's hand")
    void bouncesTargetPermanent() {
        Permanent target = addCreature(player2);
        Permanent own = addCreature(player1);
        harness.setHand(player1, List.of(new CyclonicRift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInHand(player2, "Drudge Beetle");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(own);
    }

    @Test
    @DisplayName("Cannot target a permanent you control")
    void cannotTargetOwnPermanent() {
        Permanent own = addCreature(player1);
        addCreature(player2);
        harness.setHand(player1, List.of(new CyclonicRift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, own.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Overloaded, it returns every nonland permanent you don't control and leaves lands alone")
    void overloadBouncesEveryNonlandPermanentYouDontControl() {
        Permanent first = addCreature(player2);
        Permanent second = addCreature(player2);
        Permanent own = addCreature(player1);
        Permanent opponentLand = addLand(player2);
        harness.setHand(player1, List.of(new CyclonicRift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(first, second)
                .contains(opponentLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(own);
    }

    @Test
    @DisplayName("Overload cannot be paid with only the normal mana cost available")
    void overloadRequiresTheFullOverloadCost() {
        addCreature(player2);
        harness.setHand(player1, List.of(new CyclonicRift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithOverload(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOpponentLand() {
        Permanent land = addLand(player2);
        harness.setHand(player1, List.of(new CyclonicRift()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void normalCastCannotTargetHexproof() {
        Permanent rhino = harness.addToBattlefieldAndReturn(player2, new RubblebackRhino());
        harness.setHand(player1, List.of(new CyclonicRift()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, rhino.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void overloadBouncesHexproofAndNoncreaturePermanents() {
        Permanent rhino = harness.addToBattlefieldAndReturn(player2, new RubblebackRhino());
        Permanent lantern = harness.addToBattlefieldAndReturn(player2, new ChromaticLantern());
        Permanent ownLantern = harness.addToBattlefieldAndReturn(player1, new ChromaticLantern());
        harness.setHand(player1, List.of(new CyclonicRift()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(rhino, lantern);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownLantern);
        harness.assertInHand(player2, "Rubbleback Rhino");
        harness.assertInHand(player2, "Chromatic Lantern");
    }

    @Test
    void normalCastBouncesNoncreaturePermanent() {
        Permanent lantern = harness.addToBattlefieldAndReturn(player2, new ChromaticLantern());
        Permanent other = addCreature(player2);
        harness.setHand(player1, List.of(new CyclonicRift()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, lantern.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(lantern).contains(other);
        harness.assertInHand(player2, "Chromatic Lantern");
    }

    @Test
    void overloadUsesControlAndReturnsCardsToTheirOwners() {
        Permanent ownedButNotControlled = addCreature(player2);
        gd.stolenCreatures.put(ownedButNotControlled.getId(), player1.getId());
        recordControlEffect(ownedButNotControlled, player2);
        Permanent controlledButNotOwned = addCreature(player1);
        gd.stolenCreatures.put(controlledButNotOwned.getId(), player2.getId());
        recordControlEffect(controlledButNotOwned, player1);
        harness.setHand(player1, List.of(new CyclonicRift()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(ownedButNotControlled);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(controlledButNotOwned);
        assertThat(gd.playerHands.get(player1.getId())).contains(ownedButNotControlled.getCard());
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(ownedButNotControlled.getCard());
    }

    @Test
    void targetBecomingControlledByCasterBeforeResolutionIsNotReturned() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new CyclonicRift()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        recordControlEffect(target, player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.assertNotInHand(player2, "Drudge Beetle");
    }

    @Test
    void overloadCanBeCastWithNoOpposingNonlandPermanents() {
        Permanent land = addLand(player2);
        Permanent own = addCreature(player1);
        harness.setHand(player1, List.of(new CyclonicRift()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(own);
        harness.assertInGraveyard(player1, "Cyclonic Rift");
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new DrudgeBeetle());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Island());
    }
    private void recordControlEffect(Permanent permanent, com.github.laxika.magicalvibes.model.Player controller) {
        gd.addFloatingEffect(new FloatingContinuousEffect(
                java.util.UUID.randomUUID(), "Control setup", null, controller.getId(),
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                permanent.getId(), null, null, EffectDuration.PERMANENT, 0));
    }
}
