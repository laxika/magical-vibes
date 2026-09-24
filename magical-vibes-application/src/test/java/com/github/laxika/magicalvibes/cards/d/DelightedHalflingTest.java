package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DelightedHalfling.class, GrizzlyBears.class})
class DelightedHalflingTest extends BaseCardTest {

    private ManaPool pool() {
        return gd.playerManaPools.get(player1.getId());
    }

    private GrizzlyBears legendaryBears() {
        GrizzlyBears bears = new GrizzlyBears();
        bears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        return bears;
    }

    @Test
    @DisplayName("First ability adds colorless mana")
    void tapsForColorlessMana() {
        Permanent halfling = addCreatureReady(player1, new DelightedHalfling());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(halfling.isTapped()).isTrue();
        assertThat(pool().get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability adds chosen mana restricted to legendary spells")
    void addsLegendarySpellOnlyMana() {
        addCreatureReady(player1, new DelightedHalfling());

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(pool().get(ManaColor.GREEN)).isZero();
        assertThat(pool().getLegendarySpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(legendaryBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
        assertThat(pool().getLegendarySpellOnlyMana(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Legendary-spell-only mana cannot pay for a nonlegendary spell")
    void legendarySpellOnlyManaCannotPayNonlegendarySpell() {
        addCreatureReady(player1, new DelightedHalfling());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pool().getLegendarySpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);
    }
}
