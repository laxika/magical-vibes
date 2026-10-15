package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshnodsAltar;
import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HedronCrawler;
import com.github.laxika.magicalvibes.cards.m.MassHysteria;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShadowTheHedgehog.class, ChromaticStar.class, GrizzlyBears.class,
        HedronCrawler.class, Shock.class, Slitherwisp.class, AshnodsAltar.class,
        MassHysteria.class, RagingGoblin.class, Pyroclasm.class})
class ShadowTheHedgehogTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when Shadow dies")
    void drawsWhenShadowDies() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent shadow = harness.addToBattlefieldAndReturn(player1, new ShadowTheHedgehog());

        killWithShock(player1, shadow);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws a card when another flash creature you control dies")
    void drawsWhenAllyWithFlashDies() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new ShadowTheHedgehog());
        Permanent flashCreature = harness.addToBattlefieldAndReturn(player1, new Slitherwisp());

        killWithShock(player1, flashCreature);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when another creature without flash or haste dies")
    void doesNotDrawWhenAllyLacksFlashAndHaste() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new ShadowTheHedgehog());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithShock(player1, creature);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Artifact mana gives the next spell split second")
    void artifactManaGivesSpellSplitSecond() {
        harness.addToBattlefield(player1, new ShadowTheHedgehog());
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new HedronCrawler());
        crawler.setSummoningSick(false);
        harness.setHand(player1, List.of(new ChromaticStar()));
        harness.tapPermanent(player1, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castArtifact(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A spell cast without artifact mana does not gain split second")
    void ordinaryManaDoesNotGiveSpellSplitSecond() {
        harness.addToBattlefield(player1, new ShadowTheHedgehog());
        harness.setHand(player1, List.of(new ChromaticStar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castArtifact(player1, 0);

        org.assertj.core.api.Assertions.assertThatCode(
                () -> harness.castInstant(player2, 0, player1.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    void drawsWhenAllyWithHasteDies() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new ShadowTheHedgehog());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());

        killWithShock(player1, goblin);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawWhenOpponentsHasteCreatureDies() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new ShadowTheHedgehog());
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        killWithShock(player1, goblin);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void drawsWhenAllyWithGrantedHasteDies() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new ShadowTheHedgehog());
        harness.addToBattlefield(player1, new MassHysteria());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithShock(player1, bear);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsOnlyOnceWhenAllyHasBothFlashAndHaste() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new ShadowTheHedgehog());
        harness.addToBattlefield(player1, new MassHysteria());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Slitherwisp());

        killWithShock(player1, creature);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsForShadowAndHasteAllyDyingSimultaneously() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new ShadowTheHedgehog());
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shadow the Hedgehog");
        harness.assertInGraveyard(player1, "Raging Goblin");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void manaFromSacrificedArtifactGivesSplitSecond() {
        harness.addToBattlefield(player1, new ShadowTheHedgehog());
        harness.addToBattlefield(player1, new ChromaticStar());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Chromatic Star");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void spellLosesSplitSecondWhenShadowIsSacrificedToManaAbility() {
        harness.addToBattlefield(player1, new AshnodsAltar());
        harness.addToBattlefield(player1, new ShadowTheHedgehog());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ChromaticStar()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.castArtifact(player1, 0);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Shadow the Hedgehog");
        assertThatCode(() -> harness.castInstant(player2, 0, player1.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    void opponentCanRespondAfterSplitSecondSpellResolves() {
        harness.addToBattlefield(player1, new ShadowTheHedgehog());
        Permanent crawler = harness.addToBattlefieldAndReturn(player1, new HedronCrawler());
        crawler.setSummoningSick(false);
        harness.setHand(player1, List.of(new ChromaticStar()));
        harness.tapPermanent(player1, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castArtifact(player1, 0);

        harness.passBothPriorities();

        assertThatCode(() -> harness.castInstant(player2, 0, player1.getId()))
                .doesNotThrowAnyException();
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, creature.getId());
        harness.passBothPriorities();
    }
}
