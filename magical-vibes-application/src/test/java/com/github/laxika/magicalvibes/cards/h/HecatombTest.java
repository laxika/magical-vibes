package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hecatomb.class, LlanowarElves.class, Swamp.class})
class HecatombTest extends BaseCardTest {

    private void castHecatomb() {
        harness.castFromHand(player1, new Hecatomb(), "{1}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Tapping a Swamp deals 1 damage to a target player")
    void dealsDamageToPlayer() {
        harness.addToBattlefield(player1, new Hecatomb());
        harness.addToBattlefield(player1, new Swamp());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        // The lone Swamp is tapped as a cost.
        assertThat(findPermanent(player1, "Swamp").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping a Swamp deals 1 damage to a target creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player1, new Hecatomb());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID elvesId = findPermanent(player2, "Llanowar Elves").getId();

        harness.activateAbility(player1, 0, null, elvesId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @CardUsed(GarrukWildspeaker.class)
    @DisplayName("Tapping a Swamp deals 1 damage to a target planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent planeswalker = new Permanent(new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        harness.addToBattlefield(player1, new Hecatomb());
        harness.addToBattlefield(player1, new Swamp());

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate the ability without an untapped Swamp")
    void cannotActivateWithoutUntappedSwamp() {
        harness.addToBattlefield(player1, new Hecatomb());
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        swamp.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With multiple Swamps the controller chooses which to tap")
    void multipleSwampsPromptChoice() {
        harness.addToBattlefield(player1, new Hecatomb());
        Permanent firstSwamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent secondSwamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, secondSwamp.getId());
        harness.passBothPriorities();

        assertThat(firstSwamp.isTapped()).isFalse();
        assertThat(secondSwamp.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("ETB auto-sacrifices Hecatomb when controller has fewer than four creatures")
    void etbAutoSacrificesWithoutFourCreatures() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        castHecatomb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Hecatomb");
        harness.assertInGraveyard(player1, "Hecatomb");
        // The three creatures are untouched.
        assertThat(countPermanents(player1, "Llanowar Elves")).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB accepting with exactly four creatures sacrifices all four and keeps Hecatomb")
    void etbAcceptWithFourCreatures() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        castHecatomb();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Llanowar Elves")).isEqualTo(0);
        harness.assertOnBattlefield(player1, "Hecatomb");
    }

    @Test
    @DisplayName("ETB declining sacrifices Hecatomb and keeps the creatures")
    void etbDeclineSacrificesHecatomb() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        castHecatomb();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Hecatomb");
        harness.assertInGraveyard(player1, "Hecatomb");
        assertThat(countPermanents(player1, "Llanowar Elves")).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB sacrifices exactly four creatures when controller has more than four")
    void etbSacrificesExactlyFourOfFiveCreatures() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        castHecatomb();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1,
                findPermanents(player1, "Llanowar Elves").stream()
                        .limit(4)
                        .map(Permanent::getId)
                        .toList());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Llanowar Elves")).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Hecatomb");
    }

    @Test
    @DisplayName("Can activate Hecatomb before its ETB ability resolves")
    void canActivateBeforeEtbResolves() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new Swamp());
        harness.setLife(player2, 20);

        int hecatombIndex = gd.playerBattlefields.get(player1.getId()).size();
        harness.castFromHand(player1, new Hecatomb(), "{1}{B}{B}");
        harness.passBothPriorities();

        harness.activateAbility(player1, hecatombIndex, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Hecatomb");
        assertThat(countPermanents(player1, "Llanowar Elves")).isEqualTo(3);
        assertThat(findPermanent(player1, "Swamp").isTapped()).isTrue();
    }
}
