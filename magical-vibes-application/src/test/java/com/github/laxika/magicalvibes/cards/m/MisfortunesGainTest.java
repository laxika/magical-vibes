package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MisfortunesGain.class, ForestBear.class, Island.class})
class MisfortunesGainTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys the target creature and its owner gains 4 life")
    void destroysCreatureAndOwnerGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        harness.setHand(player1, List.of(new MisfortunesGain()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int casterLifeBefore = gd.getLife(player1.getId());
        int ownerLifeBefore = gd.getLife(player2.getId());
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Forest Bear");
        harness.assertInGraveyard(player2, "Forest Bear");
        // The creature's owner (player2) gains 4 life, not the caster.
        harness.assertLife(player2, ownerLifeBefore + 4);
        harness.assertLife(player1, casterLifeBefore);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new MisfortunesGain()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles with no life gain when the target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        harness.setHand(player1, List.of(new MisfortunesGain()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int ownerLifeBefore = gd.getLife(player2.getId());
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertLife(player2, ownerLifeBefore);
    }

    @Test
    @DisplayName("Its owner gains life when the destroyed creature is controlled by another player")
    void ownerGainsLifeWhenCreatureIsControlledByAnotherPlayer() {
        ForestBear targetCard = new ForestBear();
        targetCard.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.setHand(player1, List.of(new MisfortunesGain()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int ownerLifeBefore = gd.getLife(player1.getId());
        int controllerLifeBefore = gd.getLife(player2.getId());
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Forest Bear");
        harness.assertInGraveyard(player1, "Forest Bear");
        harness.assertLife(player1, ownerLifeBefore + 4);
        harness.assertLife(player2, controllerLifeBefore);
    }

    @Test
    @DisplayName("Its owner gains life even when the target creature is indestructible")
    void ownerGainsLifeWhenCreatureIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new MisfortunesGain()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int ownerLifeBefore = gd.getLife(player2.getId());
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Forest Bear");
        harness.assertNotInGraveyard(player2, "Forest Bear");
        harness.assertLife(player2, ownerLifeBefore + 4);
    }
}
