package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GideonAllyOfZendikar;
import com.github.laxika.magicalvibes.cards.k.KozileksChanneler;
import com.github.laxika.magicalvibes.cards.s.SmiteTheMonstrous;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TouchOfTheVoid.class, GrizzlyBears.class, AvatarOfMight.class,
        KozileksChanneler.class, SmiteTheMonstrous.class, GideonAllyOfZendikar.class})
class TouchOfTheVoidTest extends BaseCardTest {

    @Test
    @DisplayName("Kills a creature and exiles it instead of putting it into the graveyard")
    void killsAndExilesCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TouchOfTheVoid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Deals 3 damage to a surviving creature and marks it for exile if it dies this turn")
    void marksSurvivorForExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new TouchOfTheVoid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(target.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Deals 3 damage to a target player")
    void dealsDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new TouchOfTheVoid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void exilesSurvivorDestroyedLaterThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksChanneler());
        harness.setHand(player1, List.of(new TouchOfTheVoid(), new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Kozilek's Channeler");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Kozilek's Channeler");
        harness.assertNotInGraveyard(player2, "Kozilek's Channeler");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void fullyPreventedDamageDoesNotExileCreatureDestroyedLater() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksChanneler());
        target.setDamagePreventionShield(3);
        harness.setHand(player1, List.of(new TouchOfTheVoid(), new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Kozilek's Channeler");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void exileReplacementExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozileksChanneler());
        harness.setHand(player1, List.of(new TouchOfTheVoid(), new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Kozilek's Channeler");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void dealsDamageToPlaneswalkerWithoutCreatureExileReplacement() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GideonAllyOfZendikar());
        target.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new TouchOfTheVoid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Gideon, Ally of Zendikar");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }
}
