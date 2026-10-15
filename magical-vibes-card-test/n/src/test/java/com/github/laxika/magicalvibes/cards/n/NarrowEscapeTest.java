package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.k.KorSanctifiers;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NarrowEscape.class, KorSanctifiers.class, Plains.class})
class NarrowEscapeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a permanent you control to its owner's hand and you gain 4 life")
    void returnsControlledPermanentAndGainsLife() {
        harness.addToBattlefield(player1, new KorSanctifiers());
        harness.setHand(player1, List.of(new NarrowEscape()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        UUID targetId = harness.getPermanentId(player1, "Kor Sanctifiers");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Kor Sanctifiers");
        harness.assertInHand(player1, "Kor Sanctifiers");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by an opponent")
    void cannotTargetOpponentsPermanent() {
        harness.addToBattlefield(player2, new KorSanctifiers());
        harness.setHand(player1, List.of(new NarrowEscape()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Kor Sanctifiers");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can return a land you control and gain life")
    void returnsLandAndGainsLife() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new NarrowEscape()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, land.getId());

        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertInHand(player1, "Plains");
        harness.assertInGraveyard(player1, "Narrow Escape");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Returns an opponent-owned permanent to its owner, while the caster gains life")
    void returnsControlledPermanentToItsOwner() {
        KorSanctifiers creature = new KorSanctifiers();
        creature.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        harness.setHand(player1, List.of(new NarrowEscape()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Kor Sanctifiers");
        harness.assertNotInHand(player1, "Kor Sanctifiers");
        harness.assertInHand(player2, "Kor Sanctifiers");
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not gain life when the target has already been returned in response")
    void doesNotGainLifeWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KorSanctifiers());
        harness.setHand(player1, List.of(new NarrowEscape(), new NarrowEscape()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertLife(player1, 24);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kor Sanctifiers");
        harness.assertInHand(player1, "Kor Sanctifiers");
        harness.assertInGraveyard(player1, "Narrow Escape");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Does not return the target or gain life if control changes before resolution")
    void doesNotResolveWhenTargetChangesController() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KorSanctifiers());
        harness.setHand(player1, List.of(new NarrowEscape()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Kor Sanctifiers");
        harness.assertNotInHand(player1, "Kor Sanctifiers");
        harness.assertNotInHand(player2, "Kor Sanctifiers");
        harness.assertInGraveyard(player1, "Narrow Escape");
        harness.assertLife(player1, 20);
    }
}
