package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrenkoMobBoss;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErthaJoFrontierMentor.class, GrizzlyBears.class, KrenkoMobBoss.class,
        ProdigalPyromancer.class})
class ErthaJoFrontierMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Mercenary with its pump ability")
    void enteringCreatesMercenary() {
        castErthaJo();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Mercenary"));
    }

    @Test
    @DisplayName("Copies an activated ability that targets a player")
    void copiesAbilityTargetingPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ErthaJoFrontierMentor());
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, battlefieldIndex(player1, pyromancer), null, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Copies the Mercenary's ability that targets a creature")
    void copiesMercenaryAbilityTargetingCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castErthaJo();
        Permanent mercenary = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Mercenary"))
                .findFirst().orElseThrow();
        mercenary.setSummoningSick(false);

        harness.activateAbility(player1, battlefieldIndex(player1, mercenary), 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not copy an activated ability without a creature or player target")
    void doesNotCopyNonTargetedAbility() {
        harness.addToBattlefield(player1, new ErthaJoFrontierMentor());
        Permanent krenko = addReady(player1, new KrenkoMobBoss());

        harness.activateAbility(player1, battlefieldIndex(player1, krenko), null, null);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("The copy can target a different player without changing the original")
    void copyCanChooseNewTarget() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ErthaJoFrontierMentor());
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, battlefieldIndex(player1, pyromancer), null, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's activated ability is not copied")
    void doesNotCopyOpponentsAbility() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ErthaJoFrontierMentor());
        Permanent pyromancer = addReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, battlefieldIndex(player2, pyromancer), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("A Mercenary copy may pump another creature you control")
    void mercenaryCopyCanChooseNewCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castErthaJo();
        Permanent ertha = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ErthaJoFrontierMentor)
                .findFirst().orElseThrow();
        Permanent mercenary = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Mercenary"))
                .findFirst().orElseThrow();
        mercenary.setSummoningSick(false);

        harness.activateAbility(player1, battlefieldIndex(player1, mercenary), 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, ertha.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(ertha.getPowerModifier()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Mercenary cannot target an opponent's creature")
    void mercenaryRejectsOpposingCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castErthaJo();
        Permanent mercenary = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Mercenary"))
                .findFirst().orElseThrow();
        mercenary.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, mercenary), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Mercenary can activate only as a sorcery")
    void mercenaryRequiresSorcerySpeed() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castErthaJo();
        Permanent mercenary = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Mercenary"))
                .findFirst().orElseThrow();
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, mercenary), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void castErthaJo() {
        harness.castFromHand(player1, new ErthaJoFrontierMentor(), "{2}{R}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return harness.getGameData().playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
