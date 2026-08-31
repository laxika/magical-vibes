package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuicksilverDragon.class, Shock.class, GrizzlyBears.class, ProdigalSorcerer.class})
class QuicksilverDragonTest extends BaseCardTest {

    @Test
    void redirectsSpellTargetingQuicksilverDragonToAnotherCreature() {
        Permanent dragon = castFaceUpDragon();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, dragon.getId());
        harness.passPriority(player1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, battlefieldIndex(player2, dragon), null, shock.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void doesNothingWhenSpellDoesNotTargetQuicksilverDragon() {
        Permanent dragon = castFaceUpDragon();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passPriority(player1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, battlefieldIndex(player2, dragon), null, shock.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void targetsSpellsButNotAbilities() {
        Permanent dragon = castFaceUpDragon();
        Permanent sorcerer = addCreatureReady(player1, new ProdigalSorcerer());

        harness.activateAbility(player1, battlefieldIndex(player1, sorcerer), null, dragon.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player2, battlefieldIndex(player2, dragon), null, sorcerer.getCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell");
    }

    private Permanent castFaceUpDragon() {
        harness.setHand(player2, List.of(new QuicksilverDragon()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player2, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player2, "Quicksilver Dragon");
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.turnFaceUp(player2, battlefieldIndex(player2, dragon));
        return dragon;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
