package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KukemssaSerpent.class, Forest.class, Island.class})
class KukemssaSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificed when controller controls no Islands")
    void sacrificedWhenNoIslands() {
        harness.setHand(player1, List.of(new KukemssaSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature -> state trigger fires
        harness.passBothPriorities(); // resolve state trigger -> sacrificed

        harness.assertNotOnBattlefield(player1, "Kukemssa Serpent");
        harness.assertInGraveyard(player1, "Kukemssa Serpent");
    }

    @Test
    @DisplayName("Survives while controller controls an Island")
    void survivesWithIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new KukemssaSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Kukemssa Serpent");
    }

    @Test
    @DisplayName("A land changed into an Island satisfies the no-Islands condition")
    void landChangedIntoIslandSatisfiesNoIslandsCondition() {
        harness.addToBattlefield(player1, new Forest());

        addCreatureReady(player2, new KukemssaSerpent());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID forestId = harness.getPermanentId(player1, "Forest");
        UUID sacrificedIslandId = harness.getPermanentId(player2, "Island");

        harness.activateAbility(player2, 0, null, forestId);
        harness.handlePermanentChosen(player2, sacrificedIslandId);
        harness.passBothPriorities();

        addCreatureReady(player1, new KukemssaSerpent());

        harness.assertOnBattlefield(player1, "Kukemssa Serpent");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability makes the targeted opponent land become an Island, overriding its type")
    void abilityMakesOpponentLandAnIsland() {
        addCreatureReady(player1, new KukemssaSerpent());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        UUID forestId = harness.getPermanentId(player2, "Forest");
        UUID sacrificedIslandId = harness.getPermanentId(player1, "Island");

        harness.activateAbility(player1, 0, null, forestId);
        harness.handlePermanentChosen(player1, sacrificedIslandId);
        harness.passBothPriorities();

        Permanent forest = gqs.findPermanentById(gd, forestId);
        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, forest);
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.ISLAND);
        assertThat(forest.getEffectiveLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Activating sacrifices an Island as a cost")
    void activationSacrificesAnIsland() {
        addCreatureReady(player1, new KukemssaSerpent());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        UUID forestId = harness.getPermanentId(player2, "Forest");
        UUID sacrificedIslandId = harness.getPermanentId(player1, "Island");

        harness.activateAbility(player1, 0, null, forestId);
        harness.handlePermanentChosen(player1, sacrificedIslandId);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .count()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("Land type override wears off at end of turn")
    void overrideWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new KukemssaSerpent());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        UUID forestId = harness.getPermanentId(player2, "Forest");
        UUID sacrificedIslandId = harness.getPermanentId(player1, "Island");

        harness.activateAbility(player1, 0, null, forestId);
        harness.handlePermanentChosen(player1, sacrificedIslandId);
        harness.passBothPriorities();

        Permanent forest = gqs.findPermanentById(gd, forestId);
        assertThat(forest.getEffectiveLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(forest.getEffectiveLandTypeOverride()).isNull();
    }

    @Test
    @DisplayName("Cannot target a land the controller controls")
    void cannotTargetOwnLand() {
        addCreatureReady(player1, new KukemssaSerpent());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Forest()); // valid target so the ability is activatable
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        UUID ownIslandId = harness.getPermanentId(player1, "Island");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownIslandId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land an opponent controls");
    }

    @Test
    @DisplayName("Cannot target a non-land permanent")
    void cannotTargetNonLand() {
        addCreatureReady(player1, new KukemssaSerpent());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Forest()); // valid target so the ability is activatable
        harness.addToBattlefield(player2, new KukemssaSerpent());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        UUID serpentId = harness.getPermanentId(player2, "Kukemssa Serpent");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, serpentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        Permanent serpent = addCreatureReady(player1, new KukemssaSerpent());

        int serpentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        declareAttackers(List.of(serpentIndex));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Island")
    void cannotAttackWhenDefenderHasNoIsland() {
        harness.addToBattlefield(player1, new Island());
        Permanent serpent = addCreatureReady(player1, new KukemssaSerpent());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        assertThatThrownBy(() -> declareAttackers(List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }
}
