package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({XantchaSleeperAgent.class, Forest.class, LilianaOfTheVeil.class,
        RayOfCommand.class, Unsummon.class})
class XantchaSleeperAgentTest extends BaseCardTest {

    @Test
    @DisplayName("Xantcha enters under an opponent's control")
    void entersUnderOpponentsControl() {
        harness.setHand(player1, List.of(new XantchaSleeperAgent()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Xantcha, Sleeper Agent");
        harness.assertOnBattlefield(player2, "Xantcha, Sleeper Agent");
    }

    @Test
    @DisplayName("Xantcha can't attack its owner or that owner's planeswalkers")
    void cannotAttackOwnerOrOwnersPlaneswalker() {
        XantchaSleeperAgent card = new XantchaSleeperAgent();
        card.setOwnerId(player1.getId());
        Permanent xantcha = addCreatureReady(player2, card);

        assertThat(als.canAttackDefender(gd, xantcha, player1.getId())).isFalse();

        LilianaOfTheVeil planeswalkerCard = new LilianaOfTheVeil();
        planeswalkerCard.setOwnerId(player1.getId());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, planeswalkerCard);

        assertThat(als.canAttackDefender(gd, xantcha, planeswalker.getId())).isFalse();
    }

    @Test
    @DisplayName("Any player may activate Xantcha's ability, making its controller lose life while the activator draws")
    void anyPlayerMayActivate() {
        XantchaSleeperAgent card = new XantchaSleeperAgent();
        card.setOwnerId(player1.getId());
        harness.addToBattlefield(player1, card);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void entersUnderOpponentControlWithoutAControlChangeTrigger() {
        harness.setHand(player1, List.of(new XantchaSleeperAgent()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Xantcha, Sleeper Agent");
        harness.assertNotOnBattlefield(player1, "Xantcha, Sleeper Agent");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerMayActivateAndBothLoseLifeAndDraw() {
        harness.addToBattlefield(player1, new XantchaSleeperAgent());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void mustAttackWhenControlledByItsOwnerAndAble() {
        XantchaSleeperAgent card = new XantchaSleeperAgent();
        card.setOwnerId(player1.getId());
        addCreatureReady(player1, card);
        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void mayDeclineToAttackWhenOnlyOpponentIsItsOwner() {
        XantchaSleeperAgent card = new XantchaSleeperAgent();
        card.setOwnerId(player1.getId());
        Permanent xantcha = addCreatureReady(player2, card);
        declareAttackers(player2, List.of());

        assertThat(xantcha.isAttacking()).isFalse();
    }

    @Test
    void usesControllerImmediatelyBeforeLeavingAfterControlChanges() {
        XantchaSleeperAgent card = new XantchaSleeperAgent();
        card.setOwnerId(player1.getId());
        Permanent xantcha = harness.addToBattlefieldAndReturn(player2, card);
        harness.setHand(player1, List.of(new RayOfCommand(), new Unsummon()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player1, 0, xantcha.getId());
        harness.assertOnBattlefield(player1, "Xantcha, Sleeper Agent");
        harness.castAndResolveInstant(player1, 0, xantcha.getId());
        harness.assertNotOnBattlefield(player1, "Xantcha, Sleeper Agent");
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Forest");
    }
}
