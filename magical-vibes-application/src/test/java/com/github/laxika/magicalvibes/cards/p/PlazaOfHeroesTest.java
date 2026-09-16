package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({PlazaOfHeroes.class, GrizzlyBears.class})
class PlazaOfHeroesTest extends BaseCardTest {

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
        Permanent plaza = harness.addToBattlefieldAndReturn(player1, new PlazaOfHeroes());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(plaza.isTapped()).isTrue();
        assertThat(pool().get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability adds chosen colored mana restricted to legendary spells")
    void addsLegendarySpellOnlyMana() {
        harness.addToBattlefield(player1, new PlazaOfHeroes());

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
    @DisplayName("Second ability's mana cannot pay for a nonlegendary spell")
    void legendarySpellOnlyManaCannotPayNonlegendarySpell() {
        harness.addToBattlefield(player1, new PlazaOfHeroes());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pool().getLegendarySpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Third ability adds mana among colors of legendary permanents")
    void addsManaAmongLegendaryPermanentColors() {
        Permanent plaza = harness.addToBattlefieldAndReturn(player1, new PlazaOfHeroes());
        harness.addToBattlefield(player1, legendaryBears());

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(plaza.isTapped()).isTrue();
        assertThat(pool().get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fourth ability exiles Plaza and protects a legendary creature until end of turn")
    void protectsLegendaryCreature() {
        Permanent plaza = harness.addToBattlefieldAndReturn(player1, new PlazaOfHeroes());
        Permanent target = harness.addToBattlefieldAndReturn(player2, legendaryBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(plaza.getCard().getId()));
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Fourth ability cannot target a nonlegendary creature")
    void cannotTargetNonlegendaryCreature() {
        Permanent plaza = harness.addToBattlefieldAndReturn(player1, new PlazaOfHeroes());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
        assertThat(plaza.isTapped()).isFalse();
        assertThat(pool().get(ManaColor.COLORLESS)).isEqualTo(3);
    }
}
