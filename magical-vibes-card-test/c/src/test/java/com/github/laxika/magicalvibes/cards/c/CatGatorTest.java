package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CatGator.class, Forest.class, Swamp.class})
class CatGatorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals damage equal to the number of Swamps you control")
    void etbDealsDamageForControlledSwamps() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CatGator());

        castAt(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Cat-Gator");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("ETB counts only Swamps controlled by Cat-Gator's controller")
    void etbIgnoresOpponentsSwamps() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CatGator());

        castAt(target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB can target a player")
    void etbCanTargetPlayer() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        castAt(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("No Swamps means no damage or lifelink life gain")
    void noSwampsDealsNoDamage() {
        castAt(player2.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB counts Swamps when the trigger resolves")
    void countsSwampsAtResolution() {
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new CatGator()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        harness.addToBattlefield(player1, new Swamp());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("ETB still deals damage with lifelink after its source leaves")
    void triggerResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new CatGator()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof CatGator);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    private void castAt(Permanent target) {
        castAt(target.getId());
    }

    private void castAt(UUID targetId) {
        harness.setHand(player1, List.of(new CatGator()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }
}
