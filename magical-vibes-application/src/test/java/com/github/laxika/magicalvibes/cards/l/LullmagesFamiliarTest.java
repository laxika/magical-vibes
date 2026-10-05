package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GnarlidColony;
import com.github.laxika.magicalvibes.cards.s.ShellShield;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LullmagesFamiliar.class, GnarlidColony.class, ShellShield.class})
class LullmagesFamiliarTest extends BaseCardTest {

    @Test
    void manaAbilityAddsGreenOrBlueMana() {
        addReadyFamiliar(player1);
        addReadyFamiliar(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("GREEN", "BLUE");
        harness.handleListChoice(player1, "GREEN");

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void gainsTwoLifeWhenControllerCastsKickedSpell() {
        addReadyFamiliar(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GnarlidColony()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    void doesNotGainLifeWhenControllerCastsNonKickedSpell() {
        addReadyFamiliar(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GnarlidColony()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void manaAbilityTapsSourceAndDoesNotUseStack() {
        Permanent familiar = addReadyFamiliar(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(familiar.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void summoningSicknessPreventsManaActivation() {
        harness.addToBattlefield(player1, new LullmagesFamiliar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void opponentsKickedSpellDoesNotTriggerLifeGain() {
        addReadyFamiliar(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GnarlidColony()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void kickedInstantTriggersLifeGainDuringOpponentsTurn() {
        Permanent familiar = addReadyFamiliar(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ShellShield()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedInstant(player1, 0, familiar.getId());
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Shell Shield");
    }

    @Test
    void eachFamiliarTriggersBeforeKickedSpellResolvesEvenWhileTapped() {
        Permanent first = addReadyFamiliar(player1);
        Permanent second = addReadyFamiliar(player1);
        first.setTapped(true);
        second.setTapped(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GnarlidColony()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(3);

        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertNotOnBattlefield(player1, "Gnarlid Colony");

        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        harness.assertNotOnBattlefield(player1, "Gnarlid Colony");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Gnarlid Colony");
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    private Permanent addReadyFamiliar(Player player) {
        return addCreatureReady(player, new LullmagesFamiliar());
    }
}
