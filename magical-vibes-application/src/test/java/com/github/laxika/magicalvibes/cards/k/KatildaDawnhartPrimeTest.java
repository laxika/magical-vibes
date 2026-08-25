package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KatildaDawnhartPrime.class, EliteVanguard.class, GrizzlyBears.class})
class KatildaDawnhartPrimeTest extends BaseCardTest {

    @Test
    @DisplayName("Human creatures gain a mana ability limited to their own colors")
    void humanCreaturesAddManaOfTheirColors() {
        Permanent katilda = addCreatureReady(player1, new KatildaDawnhartPrime());
        Permanent human = addCreatureReady(player1, new EliteVanguard());

        harness.activateAbility(player1, battlefieldIndex(katilda), 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(katilda.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();

        harness.activateAbility(player1, battlefieldIndex(human), 0, null, null);

        assertThat(human.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Human creatures do not gain Katilda's mana ability")
    void nonHumansDoNotGainManaAbility() {
        harness.addToBattlefield(player1, new KatildaDawnhartPrime());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(bears), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection from Werewolves is applied")
    void hasProtectionFromWerewolves() {
        Permanent katilda = addCreatureReady(player1, new KatildaDawnhartPrime());
        Permanent werewolf = addCreatureReady(player2, createWerewolf());
        werewolf.setSummoningSick(false);
        katilda.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("The activated ability puts a counter on each creature you control")
    void putsCountersOnOwnCreatures() {
        Permanent katilda = addCreatureReady(player1, new KatildaDawnhartPrime());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, battlefieldIndex(katilda), 0, null, null);
        harness.passBothPriorities();

        assertThat(katilda.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private static Card createWerewolf() {
        Card card = new Card();
        card.setName("Werewolf");
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}{R}");
        card.setColor(CardColor.RED);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(CardSubtype.WEREWOLF));
        return card;
    }
}
