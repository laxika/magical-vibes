package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DranasChosen.class, HadaFreeblade.class, GrizzlyBears.class})
class DranasChosenTest extends BaseCardTest {

    @Test
    @DisplayName("Cohort taps an Ally and creates a tapped Zombie")
    void cohortTapsAnAllyAndCreatesTappedZombie() {
        Permanent chosen = addCreatureReady(player1, new DranasChosen());
        Permanent ally = addCreatureReady(player1, new HadaFreeblade());

        harness.activateAbility(player1, battlefieldIndex(chosen), 0, null, null);
        harness.passBothPriorities();

        assertThat(chosen.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        List<Permanent> zombies = findPermanents(player1, "Zombie").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(zombies).hasSize(1);
        assertThat(zombies.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cohort requires another untapped Ally")
    void cohortRequiresAnotherUntappedAlly() {
        Permanent chosen = addCreatureReady(player1, new DranasChosen());
        Permanent nonAlly = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(chosen), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(chosen.isTapped()).isFalse();
        assertThat(nonAlly.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cohort can tap a summoning-sick Ally and creates the specified Zombie")
    void cohortCanTapSummoningSickAlly() {
        Permanent chosen = addCreatureReady(player1, new DranasChosen());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new DranasChosen());
        ally.setSummoningSick(true);

        harness.activateAbility(player1, battlefieldIndex(chosen), 0, null, null);

        assertThat(chosen.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();

        harness.passBothPriorities();

        List<Permanent> zombies = findPermanents(player1, "Zombie");
        assertThat(zombies).hasSize(1);
        Permanent zombie = zombies.getFirst();
        assertThat(zombie.isTapped()).isTrue();
        assertThat(zombie.getCard().isToken()).isTrue();
        assertThat(zombie.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(zombie.getCard().getColors()).containsExactly(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
        assertThat(findPermanents(player2, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick Drana's Chosen cannot activate cohort")
    void summoningSickChosenCannotActivate() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new DranasChosen());
        chosen.setSummoningSick(true);
        Permanent ally = addCreatureReady(player1, new DranasChosen());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(chosen), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(chosen.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapped Allies and opposing Allies cannot pay cohort's cost")
    void tappedAndOpposingAlliesCannotPayCost() {
        Permanent chosen = addCreatureReady(player1, new DranasChosen());
        Permanent tappedAlly = addCreatureReady(player1, new DranasChosen());
        tappedAlly.tap();
        Permanent opposingAlly = addCreatureReady(player2, new DranasChosen());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(chosen), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");

        assertThat(chosen.isTapped()).isFalse();
        assertThat(tappedAlly.isTapped()).isTrue();
        assertThat(opposingAlly.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cohort lets its controller choose which eligible Ally to tap")
    void controllerChoosesAllyToTap() {
        Permanent chosen = addCreatureReady(player1, new DranasChosen());
        Permanent firstAlly = addCreatureReady(player1, new DranasChosen());
        Permanent secondAlly = addCreatureReady(player1, new DranasChosen());

        harness.activateAbility(player1, battlefieldIndex(chosen), 0, null, null);
        harness.handlePermanentChosen(player1, secondAlly.getId());

        assertThat(chosen.isTapped()).isTrue();
        assertThat(firstAlly.isTapped()).isFalse();
        assertThat(secondAlly.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("A tapped Drana's Chosen cannot activate cohort again")
    void tappedChosenCannotActivateAgain() {
        Permanent chosen = addCreatureReady(player1, new DranasChosen());
        Permanent ally = addCreatureReady(player1, new DranasChosen());

        harness.activateAbility(player1, battlefieldIndex(chosen), 0, null, null);
        harness.passBothPriorities();
        ally.untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(chosen), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(ally.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
