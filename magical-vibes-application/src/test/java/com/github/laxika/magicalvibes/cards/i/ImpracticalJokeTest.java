package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RalZarekGuestLecturer;
import com.github.laxika.magicalvibes.cards.s.SamiteHealer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImpracticalJoke.class, HillGiant.class, AvatarOfMight.class,
        RalZarekGuestLecturer.class, SamiteHealer.class, Incinerate.class})
class ImpracticalJokeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 to target creature and makes damage unpreventable this turn")
    void deals3AndSetsUnpreventable() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player1, List.of(new ImpracticalJoke()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(targetId));

        GameData gd = harness.getGameData();
        // Hill Giant (3/3) takes 3 damage → dead
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        // Damage can't be prevented this turn
        assertThat(gd.damageCantBePreventedThisTurn).isTrue();
        assertThat(gqs.isDamagePreventable(gd)).isFalse();
    }

    @Test
    @DisplayName("Can be cast with no target; still makes damage unpreventable this turn")
    void castWithNoTargetSetsUnpreventable() {
        harness.setHand(player1, List.of(new ImpracticalJoke()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, List.of());

        GameData gd = harness.getGameData();
        assertThat(gd.damageCantBePreventedThisTurn).isTrue();
        harness.assertInGraveyard(player1, "Impractical Joke");
    }

    @Test
    @DisplayName("Deals exactly 3 to a surviving creature")
    void deals3ToSurvivor() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        UUID targetId = harness.getPermanentId(player2, "Avatar of Might");
        harness.setHand(player1, List.of(new ImpracticalJoke()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(targetId));

        harness.assertOnBattlefield(player2, "Avatar of Might");
        assertThat(avatar.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void dealsThreeDamageToPlaneswalker() {
        Permanent ral = harness.addToBattlefieldAndReturn(player2, new RalZarekGuestLecturer());
        ral.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new ImpracticalJoke()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(ral.getId()));

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Ral Zarek, Guest Lecturer");
    }

    @Test
    void preventionIsDisabledBeforeItsOwnDamage() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        addCreatureReady(player1, new SamiteHealer());
        harness.activateAbility(player1, 0, null, giant.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new ImpracticalJoke()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(giant.getId()));

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void illegalSoleTargetStopsThePreventionEffectToo() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ImpracticalJoke()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, List.of(giant.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(giant);

        harness.passBothPriorities();

        assertThat(gd.damageCantBePreventedThisTurn).isFalse();
        harness.assertInGraveyard(player1, "Impractical Joke");
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new ImpracticalJoke()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void noTargetMakesOpponentsDamageToPlayersUnpreventable() {
        harness.setHand(player1, List.of(new ImpracticalJoke()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, List.of());
        addCreatureReady(player1, new SamiteHealer());
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    void preventionReturnsOnTheNextTurn() {
        harness.setHand(player1, List.of(new ImpracticalJoke()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.setLibrary(player2, List.of(new HillGiant()));

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.damageCantBePreventedThisTurn).isFalse();
        assertThat(gqs.isDamagePreventable(gd)).isTrue();
    }
}
