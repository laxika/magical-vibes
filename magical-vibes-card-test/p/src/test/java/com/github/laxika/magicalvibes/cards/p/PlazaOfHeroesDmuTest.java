package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DanithaCapashenParagon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ToriDAvenantFuryRider;
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

@CardUsed({PlazaOfHeroes.class, DanithaCapashenParagon.class, GrizzlyBears.class, ToriDAvenantFuryRider.class})
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
    @DisplayName("The fourth ability protects a legendary creature and exiles the land")
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
    @DisplayName("The fourth ability only targets legendary creatures")
    void rejectsNonlegendaryCreatureTarget() {
        harness.addToBattlefield(player1, new PlazaOfHeroes());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    void choosesOnlyColorsAmongControlledLegendaryPermanents() {
        harness.addToBattlefield(player1, new PlazaOfHeroes());
        harness.addToBattlefield(player1, new ToriDAvenantFuryRider());

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThatThrownBy(() -> harness.handleListChoice(player1, "GREEN"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getLegendarySpellOnlyMana(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void producesNoManaWithoutControlledColoredLegendaryPermanents() {
        Permanent plaza = harness.addToBattlefieldAndReturn(player1, new PlazaOfHeroes());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new ToriDAvenantFuryRider());

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(plaza.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exilesAsCostBeforeProtectingOpponentsLegendaryCreature() {
        Permanent plaza = harness.addToBattlefieldAndReturn(player1, new PlazaOfHeroes());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ToriDAvenantFuryRider());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, null, target.getId());

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(plaza.getCard().getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void legendarySpellManaCannotPayForProtectionAbility() {
        Permanent plaza = harness.addToBattlefieldAndReturn(player1, new PlazaOfHeroes());
        harness.addToBattlefield(player1, new PlazaOfHeroes());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ToriDAvenantFuryRider());
        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "RED");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(plaza.isTapped()).isFalse();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getLegendarySpellOnlyMana(ManaColor.RED)).isEqualTo(1);
    }
}
