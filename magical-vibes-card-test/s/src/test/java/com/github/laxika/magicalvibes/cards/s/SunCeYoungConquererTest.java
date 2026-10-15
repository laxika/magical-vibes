package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunCeYoungConquerer.class, ShuDefender.class, Forest.class})
class SunCeYoungConquererTest extends BaseCardTest {

    private void castSunCe() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SunCeYoungConquerer(), "{3}{U}{U}");
    }

    @Test
    @DisplayName("ETB triggers a may prompt when a creature is on the battlefield")
    void etbTriggersMayPrompt() {
        harness.addToBattlefield(player2, new ShuDefender());
        castSunCe();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Shu Defender"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting returns the target creature to its owner's hand")
    void acceptingBouncesTargetCreature() {
        harness.addToBattlefield(player2, new ShuDefender());
        UUID shuDefenderId = harness.getPermanentId(player2, "Shu Defender");
        castSunCe();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, shuDefenderId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Shu Defender");
        harness.assertInHand(player2, "Shu Defender");
    }

    @Test
    @DisplayName("Declining the may does not bounce anything")
    void decliningMayDoesNotBounce() {
        harness.addToBattlefield(player2, new ShuDefender());
        castSunCe();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Shu Defender"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Shu Defender");
    }

    @Test
    @DisplayName("Sun Ce enters the battlefield after resolution")
    void sunCeEntersBattlefield() {
        harness.addToBattlefield(player2, new ShuDefender());
        UUID shuDefenderId = harness.getPermanentId(player2, "Shu Defender");
        castSunCe();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, shuDefenderId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Sun Ce, Young Conquerer");
    }

    @Test
    @DisplayName("The ETB ability can target Sun Ce itself")
    void canTargetItself() {
        castSunCe();
        harness.passBothPriorities();

        UUID sunCeId = harness.getPermanentId(player1, "Sun Ce, Young Conquerer");
        harness.handlePermanentChosen(player1, sunCeId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Sun Ce, Young Conquerer");
        harness.assertInHand(player1, "Sun Ce, Young Conquerer");
    }

    @Test
    @DisplayName("The ETB target selection includes creatures but excludes noncreature permanents")
    void targetSelectionOnlyIncludesCreatures() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        castSunCe();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        UUID sunCeId = harness.getPermanentId(player1, "Sun Ce, Young Conquerer");
        assertThat(choice.validIds()).contains(sunCeId).doesNotContain(forest.getId());

        harness.handlePermanentChosen(player1, sunCeId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("A creature controlled by another player returns to its owner's hand")
    void stolenCreatureReturnsToOwner() {
        ShuDefender card = new ShuDefender();
        card.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        castSunCe();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Shu Defender");
        harness.assertInHand(player2, "Shu Defender");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("The ETB ability still resolves after Sun Ce leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShuDefender());
        castSunCe();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        Permanent source = findPermanent(player1, "Sun Ce, Young Conquerer");
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Shu Defender");
        harness.assertInHand(player2, "Shu Defender");
    }

    @Test
    @DisplayName("The ETB ability does nothing when its target leaves before resolution")
    void removedTargetDoesNotReturnFromGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ShuDefender());
        castSunCe();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    @DisplayName("Sun Ce cannot be blocked by a creature without horsemanship")
    void cannotBeBlockedByCreatureWithoutHorsemanship() {
        Permanent blocker = addCreatureReady(player2, new ShuDefender());
        Permanent attacker = addCreatureReady(player1, new SunCeYoungConquerer());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horsemanship");
    }

    @Test
    @DisplayName("Sun Ce can be blocked by a creature with horsemanship")
    void canBeBlockedByCreatureWithHorsemanship() {
        Permanent blocker = addCreatureReady(player2, new SunCeYoungConquerer());
        Permanent attacker = addCreatureReady(player1, new SunCeYoungConquerer());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
