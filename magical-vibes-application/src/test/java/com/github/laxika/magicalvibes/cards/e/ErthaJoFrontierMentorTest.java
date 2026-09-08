package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KrenkoMobBoss;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void castErthaJo() {
        harness.setHand(player1, List.of(new ErthaJoFrontierMentor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return harness.getGameData().playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
