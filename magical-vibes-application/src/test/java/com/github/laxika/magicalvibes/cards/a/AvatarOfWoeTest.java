package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.cards.u.UncleIstvan;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvatarOfWoe.class, Squire.class, TormodsCrypt.class, Dodecapod.class, UncleIstvan.class})
class AvatarOfWoeTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast for {B}{B} when there are ten creature cards across all graveyards")
    void costsLessWithTenCreatureCardsAcrossGraveyards() {
        harness.setGraveyard(player1, IntStream.range(0, 9).<Card>mapToObj(i -> new Squire()).toList());
        harness.setGraveyard(player2, List.of(new Squire()));

        harness.castFromHand(player1, new AvatarOfWoe(), "{B}{B}");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not reduce its cost for noncreature cards in graveyards")
    void doesNotCountNoncreatureCards() {
        harness.setGraveyard(player1, IntStream.range(0, 9).<Card>mapToObj(i -> new Squire()).toList());
        harness.setGraveyard(player2, List.of(new TormodsCrypt()));

        assertThatThrownBy(() -> harness.castFromHand(player1, new AvatarOfWoe(), "{B}{B}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Nine creature cards do not reduce the casting cost")
    void costsFullManaBelowThreshold() {
        harness.setGraveyard(player2, IntStream.range(0, 9).<Card>mapToObj(i -> new Squire()).toList());

        harness.castFromHand(player1, new AvatarOfWoe(), "{6}{B}{B}");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Avatar of Woe");
    }

    @Test
    @DisplayName("Ten creature cards in only the opponent's graveyard reduce the cost")
    void countsCreaturesInOpponentsGraveyardAlone() {
        harness.setGraveyard(player2, IntStream.range(0, 10).<Card>mapToObj(i -> new Squire()).toList());

        harness.castFromHand(player1, new AvatarOfWoe(), "{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avatar of Woe");
    }

    @Test
    @DisplayName("Artifact creature cards count toward the graveyard threshold")
    void countsArtifactCreatureCards() {
        harness.setGraveyard(player1, IntStream.range(0, 9).<Card>mapToObj(i -> new Squire()).toList());
        harness.setGraveyard(player2, List.of(new Dodecapod()));

        harness.castFromHand(player1, new AvatarOfWoe(), "{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avatar of Woe");
    }

    @Test
    @DisplayName("More than ten creature cards do not reduce the required black mana")
    void reductionDoesNotRemoveColoredMana() {
        harness.setGraveyard(player1, IntStream.range(0, 11).<Card>mapToObj(i -> new Squire()).toList());

        assertThatThrownBy(() -> harness.castFromHand(player1, new AvatarOfWoe(), "{B}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Avatar of Woe can target and destroy itself")
    void canDestroyItself() {
        Permanent avatar = addCreatureReady(player1, new AvatarOfWoe());

        harness.activateAbility(player1, 0, null, avatar.getId());
        assertThat(avatar.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Avatar of Woe");
        harness.assertInGraveyard(player1, "Avatar of Woe");
    }

    @Test
    @DisplayName("A summoning-sick Avatar cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player1, new AvatarOfWoe());
        avatar.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new Squire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(avatar.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Squire");
    }

    @Test
    @DisplayName("Tap ability destroys a target creature without allowing regeneration")
    void destroysTargetCreatureWithoutRegeneration() {
        Permanent avatar = addCreatureReady(player1, new AvatarOfWoe());
        Permanent target = addCreatureReady(player2, new Squire());
        target.setRegenerationShield(1);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(avatar.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(avatar);
        harness.assertNotOnBattlefield(player2, "Squire");
        harness.assertInGraveyard(player2, "Squire");
    }

    @Test
    @DisplayName("Tap ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new AvatarOfWoe());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TormodsCrypt());
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Fear allows only black or artifact creature blockers")
    void fearRestrictsBlockersToBlackOrArtifactCreatures() {
        Permanent avatar = addCreatureReady(player1, new AvatarOfWoe());
        avatar.setAttacking(true);
        Permanent whiteBlocker = addCreatureReady(player2, new Squire());
        Permanent blackBlocker = addCreatureReady(player2, new UncleIstvan());
        Permanent artifactBlocker = addCreatureReady(player2, new Dodecapod());
        List<Permanent> defenderBattlefield = gd.playerBattlefields.get(player2.getId());

        assertThat(bls.canBlockAttacker(gd, whiteBlocker, avatar, defenderBattlefield)).isFalse();
        assertThat(bls.canBlockAttacker(gd, blackBlocker, avatar, defenderBattlefield)).isTrue();
        assertThat(bls.canBlockAttacker(gd, artifactBlocker, avatar, defenderBattlefield)).isTrue();
    }
}
