package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DanithaCapashenParagon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlazaOfHeroes.class, DanithaCapashenParagon.class, GrizzlyBears.class})
class PlazaOfHeroesDmuTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds colorless mana")
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new PlazaOfHeroes());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanent(player1, "Plaza of Heroes").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability adds a chosen colored legendary-only mana")
    void addsChosenLegendaryOnlyMana() {
        harness.addToBattlefield(player1, new PlazaOfHeroes());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).getLegendarySpellOnlyMana(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Legendary-only colored mana pays for a legendary spell")
    void paysForLegendarySpell() {
        harness.addToBattlefield(player1, new PlazaOfHeroes());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "WHITE");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new DanithaCapashenParagon()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Danitha Capashen, Paragon")).isNotNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getLegendarySpellOnlyMana(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Legendary-only colored mana cannot pay for a nonlegendary spell")
    void cannotPayForNonlegendarySpell() {
        harness.addToBattlefield(player1, new PlazaOfHeroes());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getLegendarySpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The third ability protects a legendary creature and exiles the land")
    void protectsLegendaryCreature() {
        harness.addToBattlefield(player1, new PlazaOfHeroes());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DanithaCapashenParagon());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Plaza of Heroes")).isEmpty();
        assertThat(target.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The third ability only targets legendary creatures")
    void rejectsNonlegendaryCreatureTarget() {
        harness.addToBattlefield(player1, new PlazaOfHeroes());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }
}
