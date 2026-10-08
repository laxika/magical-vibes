package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AdagiaWindsweptBastion;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.e.EvendoWakingHaven;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.k.KavaronMemorialWorld;
import com.github.laxika.magicalvibes.cards.s.SusurSecundiVoidAltar;
import com.github.laxika.magicalvibes.cards.u.UthrosTitanicGodcore;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VvvizaOrbitalOverseer.class, GrizzlyBears.class, Clone.class, Forest.class, SoulWarden.class,
        AdagiaWindsweptBastion.class, EvendoWakingHaven.class, KavaronMemorialWorld.class,
        SusurSecundiVoidAltar.class, UthrosTitanicGodcore.class})
class VvvizaOrbitalOverseerTest extends BaseCardTest {

    @Test
    void enteringOffersEveryPlanetFromTheSpellbook() {
        harness.enterBattlefieldAndReturn(player1, new VvvizaOrbitalOverseer());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleListChoice(player1, "Adagia, Windswept Bastion");

        assertThat(findPermanent(player1, "Adagia, Windswept Bastion")).isNotNull();
    }

    @Test
    void attackingCreatesAFlyingLanderCreature() {
        addCreatureReady(player1, new VvvizaOrbitalOverseer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();

        Permanent lander = findPermanent(player1, "Lander");
        assertThat(gqs.isCreature(gd, lander)).isTrue();
        assertThat(lander.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, lander)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lander)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, lander, Keyword.FLYING)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Evendo, Waking Haven", "Kavaron, Memorial World",
            "Susur Secundi, Void Altar", "Uthros, Titanic Godcore"})
    void canConjureEachOtherPlanet(String name) {
        harness.enterBattlefieldAndReturn(player1, new VvvizaOrbitalOverseer());
        resolveAllTriggers();
        harness.handleListChoice(player1, name);

        assertThat(findPermanents(player1, name)).hasSize(1);
        assertThat(findPermanent(player1, name).isTapped()).isTrue();
        assertThat(findPermanents(player2, name)).isEmpty();
        harness.assertNotInHand(player1, name);
    }

    @Test
    void multipleAttackersCreateOnlyOneLander() {
        Permanent overseer = addCreatureReady(player1, new VvvizaOrbitalOverseer());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(overseer),
                gd.playerBattlefields.get(player1.getId()).indexOf(ally)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
        assertThat(findPermanent(player1, "Lander").isAttacking()).isFalse();
        assertThat(findPermanent(player1, "Lander").isTapped()).isFalse();
    }

    @Test
    void opposingAttackDoesNotCreateALander() {
        addCreatureReady(player1, new VvvizaOrbitalOverseer());
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(enemy)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(findPermanents(player2, "Lander")).isEmpty();
    }

    @Test
    void newlyCreatedCreatureLanderCannotPayATapCost() {
        Permanent lander = createLander();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lander), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Lander")).containsExactly(lander);
    }

    @Test
    void landerSacrificesToFindABasicLandTappedAndShuffle() {
        Permanent lander = createLander();
        lander.setSummoningSick(false);
        Forest forest = new Forest();
        AdagiaWindsweptBastion nonbasic = new AdagiaWindsweptBastion();
        harness.setLibrary(player1, List.of(nonbasic, forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lander), null, null);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasic);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void copyingLanderDoesNotCopyItsAnimationOrGrantedFlying() {
        Permanent lander = createLander();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, lander.getId());

        Permanent copy = findPermanents(player1, "Lander").stream()
                .filter(permanent -> !permanent.getId().equals(lander.getId()))
                .findFirst().orElseThrow();
        assertThat(copy.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.isCreature(gd, copy)).isFalse();
        assertThat(gqs.hasKeyword(gd, copy, Keyword.FLYING)).isFalse();
    }

    @Test
    void landerEntersAsANoncreatureBeforeItBecomesACreature() {
        addCreatureReady(player1, new VvvizaOrbitalOverseer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SoulWarden());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
        harness.assertLife(player1, lifeBefore);
    }

    private Permanent createLander() {
        addCreatureReady(player1, new VvvizaOrbitalOverseer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveAllTriggers();
        return findPermanent(player1, "Lander");
    }
}
