package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PowerArmor.class, RagingKavu.class, Forest.class, Island.class, Mountain.class,
        AshayaSoulOfTheWild.class})
class PowerArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the target by one for each distinct basic land type controlled")
    void boostsTargetByDomain() {
        setupBattlefield();

        UUID targetId = findPermanent(player1, "Raging Kavu").getId();
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Power Armor").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        Permanent kavu = findPermanent(player1, "Raging Kavu");
        assertThat(kavu.getPowerModifier()).isEqualTo(3);
        assertThat(kavu.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target an opponent's creature using the controller's domain count")
    void boostsOpponentCreatureUsingControllerDomain() {
        setupBattlefield();
        Permanent opponentKavu = harness.addToBattlefieldAndReturn(player2, new RagingKavu());

        harness.activateAbility(player1, 0, null, opponentKavu.getId());
        harness.passBothPriorities();

        assertThat(opponentKavu.getPowerModifier()).isEqualTo(3);
        assertThat(opponentKavu.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        setupBattlefield();

        UUID targetId = findPermanent(player1, "Raging Kavu").getId();
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        Permanent kavu = findPermanent(player1, "Raging Kavu");
        assertThat(kavu.getPowerModifier()).isEqualTo(0);
        assertThat(kavu.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        setupBattlefield();

        UUID forestId = findPermanent(player1, "Forest").getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No lands means no boost, even when the opponent controls lands")
    void zeroDomainDoesNotCountOpponentLands() {
        setupBattlefield();
        gd.playerBattlefields.get(player1.getId()).removeIf(p ->
                p.getCard() instanceof Forest || p.getCard() instanceof Island
                        || p.getCard() instanceof Mountain);
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Mountain());
        Permanent target = findPermanent(player1, "Raging Kavu");

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(findPermanent(player1, "Power Armor").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Domain is counted at resolution and the resolved boost stays fixed")
    void countsDomainAtResolutionAndKeepsBoostFixed() {
        setupBattlefield();
        Permanent island = findPermanent(player1, "Island");
        gd.playerBattlefields.get(player1.getId()).remove(island);
        Permanent target = findPermanent(player1, "Raging Kavu");

        harness.activateAbility(player1, 0, null, target.getId());
        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Island"));
        harness.runStateBasedActions();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("The activated ability resolves after Power Armor leaves the battlefield")
    void abilityResolvesWithoutSource() {
        setupBattlefield();
        Permanent armor = findPermanent(player1, "Power Armor");
        Permanent target = findPermanent(player1, "Raging Kavu");

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(armor);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate a tapped Power Armor")
    void cannotActivateWhileTapped() {
        setupBattlefield();
        findPermanent(player1, "Power Armor").tap();
        UUID targetId = findPermanent(player1, "Raging Kavu").getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate without three mana")
    void cannotActivateWithoutEnoughMana() {
        setupBattlefield();
        gd.playerManaPools.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = findPermanent(player1, "Raging Kavu").getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Power Armor").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Domain counts creatures that Ashaya makes into Forest lands")
    void countsCreaturesMadeIntoForestLands() {
        setupBattlefield();
        gd.playerBattlefields.get(player1.getId()).removeIf(p ->
                p.getCard() instanceof Forest || p.getCard() instanceof Island
                        || p.getCard() instanceof Mountain);
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());
        Permanent target = findPermanent(player1, "Raging Kavu");
        assertThat(gqs.isLand(gd, target)).isTrue();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    private void setupBattlefield() {
        harness.addToBattlefield(player1, new PowerArmor());
        harness.addToBattlefield(player1, new RagingKavu());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
