package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FieldOfTheDead.class, EvolvingWilds.class, Forest.class, Island.class,
        Swamp.class, Mountain.class, Plains.class})
class FieldOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and produces colorless mana")
    void entersTappedAndProducesColorlessMana() {
        playLand(new FieldOfTheDead());

        Permanent field = findPermanent(player1, "Field of the Dead");
        assertThat(field.isTapped()).isTrue();

        field.untap();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(field), null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates a Zombie when a seventh differently named land enters")
    void createsZombieForSeventhDifferentLand() {
        addSixDifferentLandsIncludingField();

        playLand(new EvolvingWilds());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("The Field of the Dead itself triggers when it enters as the seventh differently named land")
    void triggersWhenFieldItselfEntersAsSeventhLand() {
        addSixDifferentLandsWithoutField();

        playLand(new FieldOfTheDead());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger with seven lands if only six names are represented")
    void doesNotTriggerForDuplicateLandName() {
        addSixDifferentLandsIncludingField();

        playLand(new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("The created Zombie is an untapped 2/2 black Zombie token")
    void createsCorrectZombieToken() {
        addSixDifferentLandsIncludingField();

        playLand(new EvolvingWilds());
        resolveAllTriggers();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getCard().isToken()).isTrue();
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
        assertThat(zombie.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Field's own entry trigger also rechecks the seven names at resolution")
    void ownEntryTriggerRechecksDistinctNames() {
        addSixDifferentLandsWithoutField();
        harness.setLibrary(player1, List.of());
        playLand(new FieldOfTheDead());
        assertThat(gd.stack).hasSize(1);

        Permanent wilds = findPermanent(player1, "Evolving Wilds");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(wilds), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("A duplicate-name land still triggers when seven different land names are already present")
    void duplicateLandTriggersWithSevenNamesAlreadyPresent() {
        addSixDifferentLandsIncludingField();
        harness.addToBattlefield(player1, new EvolvingWilds());

        playLand(new Forest());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("Losing the seventh land name before resolution prevents the Zombie")
    void rechecksDistinctNamesAtResolution() {
        addSixDifferentLandsIncludingField();
        harness.setLibrary(player1, List.of());
        playLand(new EvolvingWilds());
        assertThat(gd.stack).hasSize(1);

        Permanent wilds = findPermanent(player1, "Evolving Wilds");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(wilds), null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        harness.assertInGraveyard(player1, "Evolving Wilds");
    }

    @Test
    @DisplayName("Two Fields each trigger but share a single land name")
    void multipleFieldsTriggerIndependently() {
        addSixDifferentLandsIncludingField();
        harness.addToBattlefield(player1, new FieldOfTheDead());

        playLand(new EvolvingWilds());

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Zombie")).hasSize(2);
        assertThat(findPermanents(player2, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("Field entering below seven names does not trigger")
    void fieldDoesNotTriggerBelowThreshold() {
        harness.addToBattlefield(player1, new Forest());

        playLand(new FieldOfTheDead());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's land entry does not trigger Field")
    void opponentLandDoesNotTrigger() {
        addSixDifferentLandsIncludingField();
        harness.addToBattlefield(player1, new EvolvingWilds());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's differently named land does not contribute to the seven names")
    void opponentLandDoesNotCountTowardThreshold() {
        addSixDifferentLandsIncludingField();
        harness.addToBattlefield(player2, new EvolvingWilds());

        playLand(new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    private void addSixDifferentLandsIncludingField() {
        harness.addToBattlefield(player1, new FieldOfTheDead());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());
    }

    private void addSixDifferentLandsWithoutField() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new EvolvingWilds());
    }

    private void playLand(com.github.laxika.magicalvibes.model.Card land) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(land));
        harness.playLand(player1, 0);
    }
}
