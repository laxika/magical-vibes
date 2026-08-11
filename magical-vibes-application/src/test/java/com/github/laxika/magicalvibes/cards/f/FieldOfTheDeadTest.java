package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
