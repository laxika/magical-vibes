package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GlacialChasm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MirrorGallery;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Skullcrack;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheMindskinner.class, Shock.class, GrizzlyBears.class, ZuranSpellcaster.class,
        MirrorGallery.class, Skullcrack.class, GlacialChasm.class})
class TheMindskinnerTest extends BaseCardTest {

    @Test
    void replacesSpellDamageWithMill() {
        addCreatureReady(player1, new TheMindskinner());
        harness.setLibrary(player2, library(5));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    void replacesActivatedAbilityDamageWithMill() {
        addCreatureReady(player1, new TheMindskinner());
        addCreatureReady(player1, new ZuranSpellcaster());
        harness.setLibrary(player2, library(5));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    @Test
    void replacesCombatDamageWithMill() {
        addCreatureReady(player1, new TheMindskinner());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setLibrary(player2, library(5));
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    void doesNotReplaceDamageToItsControllerOrDamageFromAnOpponent() {
        addCreatureReady(player1, new TheMindskinner());
        harness.setLibrary(player1, library(5));
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    void multipleMindskinnersDoNotMultiplyTheReplacement() {
        addCreatureReady(player1, new MirrorGallery());
        addCreatureReady(player1, new TheMindskinner());
        addCreatureReady(player1, new TheMindskinner());
        harness.setLibrary(player2, library(5));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    void cannotBeBlocked() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new TheMindskinner());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void millsEvenWhenSpellDamageCannotBePrevented() {
        addCreatureReady(player1, new TheMindskinner());
        harness.setLibrary(player2, library(5));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Skullcrack()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    void millsEvenWhenCombatDamageCannotBePrevented() {
        addCreatureReady(player1, new TheMindskinner());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player2, library(5));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Skullcrack()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 18);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void eachMindskinnerMillsWhenDamageCannotBePrevented() {
        harness.addToBattlefield(player1, new MirrorGallery());
        addCreatureReady(player1, new TheMindskinner());
        addCreatureReady(player1, new TheMindskinner());
        harness.setLibrary(player2, library(8));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Skullcrack()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
    }

    @Test
    void affectedPlayerChoosesBetweenMindskinnerAndOtherPrevention() {
        addCreatureReady(player1, new TheMindskinner());
        harness.addToBattlefield(player2, new GlacialChasm());
        harness.setLibrary(player2, library(5));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        harness.assertLife(player2, 20);
    }

    @Test
    void replacesItsOwnCombatDamageWithMill() {
        Permanent attacker = addCreatureReady(player1, new TheMindskinner());
        attacker.setAttacking(true);
        harness.setLibrary(player2, library(12));
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);
    }

    @Test
    void stillPreventsAllDamageWhenTheLibraryHasTooFewCards() {
        Permanent attacker = addCreatureReady(player1, new TheMindskinner());
        attacker.setAttacking(true);
        harness.setLibrary(player2, library(1));
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void doesNotReplaceDamageToAnOpponentsCreature() {
        addCreatureReady(player1, new TheMindskinner());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player2, library(5));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    void doesNotReplaceAnOpponentsDamageToThemselves() {
        addCreatureReady(player1, new TheMindskinner());
        harness.setLibrary(player2, library(5));
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    void stopsReplacingDamageAfterLeavingTheBattlefield() {
        Permanent mindskinner = addCreatureReady(player1, new TheMindskinner());
        harness.setLibrary(player2, library(5));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, mindskinner.getId());
        harness.assertNotOnBattlefield(player1, "The Mindskinner");

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    private List<Card> library(int count) {
        return IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList();
    }
}
