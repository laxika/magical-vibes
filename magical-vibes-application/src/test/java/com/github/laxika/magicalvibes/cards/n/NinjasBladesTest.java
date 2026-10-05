package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NinjasBlades.class, Forest.class, GrizzlyBears.class})
class NinjasBladesTest extends BaseCardTest {

    @Test
    @DisplayName("Job select creates and equips a Hero, making it a Ninja")
    void jobSelectCreatesAndEquipsHero() {
        castBlades();

        Permanent blades = findPermanent(player1, "Ninja's Blades");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(blades.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).contains(CardSubtype.NINJA);
    }

    @Test
    @DisplayName("Combat damage draws, discards, and makes the damaged player lose the discarded card's mana value")
    void combatDamageRummagesAndLosesLife() {
        castBlades();
        Permanent hero = findPermanent(player1, "Hero");
        hero.setSummoningSick(false);
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, List.of(new Forest()));

        int heroIndex = gd.playerBattlefields.get(player1.getId()).indexOf(hero);
        declareAttackers(List.of(heroIndex));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Discarding a land causes no additional life loss")
    void discardingLandLosesNoLife() {
        castBlades();
        Permanent hero = findPermanent(player1, "Hero");
        hero.setSummoningSick(false);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new NinjasBlades()));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(hero)));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Ninja's Blades");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Equip moves the bonuses and granted combat ability to another creature")
    void equipMovesBonusesAndCombatAbility() {
        castBlades();
        Permanent hero = findPermanent(player1, "Hero");
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blades = findPermanent(player1, "Ninja's Blades");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blades),
                null, bear.getId());
        harness.passBothPriorities();

        assertThat(blades.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).doesNotContain(CardSubtype.NINJA);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bear)).contains(CardSubtype.BEAR, CardSubtype.NINJA);

        bear.setSummoningSick(false);
        harness.setHand(player1, List.of(new NinjasBlades()));
        harness.setLibrary(player1, List.of(new Forest()));
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bear)));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Ninja's Blades");
        harness.assertInHand(player1, "Forest");
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("The equipped creature's controller draws and discards when Equipment has a different controller")
    void creatureControllerControlsGrantedTrigger() {
        Permanent blades = harness.addToBattlefieldAndReturn(player1, new NinjasBlades());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blades.setAttachedTo(bear.getId());
        bear.setSummoningSick(false);
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new NinjasBlades()));
        harness.setLibrary(player2, List.of(new Forest()));

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(bear)));
        resolveCombat(player2);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Ninja's Blades");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }

    private void castBlades() {
        harness.setHand(player1, List.of(new NinjasBlades()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
