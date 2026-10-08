package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FledglingMawcor;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WipeAway.class, Island.class, ThinkTwice.class, FledglingMawcor.class})
class WipeAwayTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a stolen permanent to its owner rather than its controller")
    void returnsStolenPermanentToOwner() {
        FledglingMawcor card = new FledglingMawcor();
        card.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, card);
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.setHand(player1, List.of(new WipeAway()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Fledgling Mawcor");
        harness.assertInHand(player1, "Fledgling Mawcor");
        harness.assertNotInHand(player2, "Fledgling Mawcor");
    }

    @Test
    @DisplayName("Split second allows turning a morph face up before the target is returned")
    void allowsTurningMorphFaceUp() {
        harness.setHand(player1, List.of(new FledglingMawcor(), new WipeAway()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.turnFaceUp(player1, 0);

        assertThat(target.isFaceDown()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Fledgling Mawcor");
        harness.assertInHand(player1, "Fledgling Mawcor");
        harness.assertInGraveyard(player1, "Wipe Away");
    }

    @Test
    @DisplayName("Split second stops restricting spells and abilities after resolution")
    void splitSecondEndsAfterResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        addCreatureReady(player2, new FledglingMawcor());
        harness.setHand(player1, List.of(new WipeAway()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new ThinkTwice()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
        harness.castAndResolveInstant(player2, 0);

        harness.assertInGraveyard(player2, "Think Twice");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Returns any target permanent to its owner's hand")
    void returnsAnyTargetPermanentToOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new WipeAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player2, "Island");
        harness.assertInGraveyard(player1, "Wipe Away");
    }

    @Test
    @DisplayName("Fizzles if the target permanent leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new WipeAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));

        harness.passBothPriorities();

        harness.assertInHand(player2, "Island");
        harness.assertInGraveyard(player1, "Wipe Away");
    }

    @Test
    @DisplayName("Split second prevents spells and non-mana activated abilities")
    void splitSecondPreventsSpellsAndNonManaAbilities() {
        Permanent mawcor = addCreatureReady(player2, new FledglingMawcor());
        harness.setHand(player1, List.of(new WipeAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, mawcor.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mawcor.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Split second still allows mana abilities")
    void splitSecondStillAllowsManaAbilities() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent mawcor = addCreatureReady(player2, new FledglingMawcor());
        harness.setHand(player1, List.of(new WipeAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, mawcor.getId());
        harness.tapPermanent(player2, 0);

        assertThat(island.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
