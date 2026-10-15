package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KasminaEnigmaSage;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SelflessGlyphweaver.class, Forest.class, GrizzlyBears.class, NicolBolasPlaneswalker.class, KasminaEnigmaSage.class})
class SelflessGlyphweaverTest extends BaseCardTest {

    @Test
    void frontFaceResolvesAsACreatureAndCanActivateImmediately() {
        harness.castFromHand(player1, new SelflessGlyphweaver(), "{2}{W}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Selfless Glyphweaver");

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Selfless Glyphweaver");
        harness.assertNotInGraveyard(player1, "Selfless Glyphweaver");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Selfless Glyphweaver");
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exileIsPaidImmediatelyButProtectionAppliesOnlyAtResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SelflessGlyphweaver());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SelflessGlyphweaver());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(source.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new SelflessGlyphweaver());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.INDESTRUCTIBLE)).isTrue();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new SelflessGlyphweaver());
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void indestructibleExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new SelflessGlyphweaver());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SelflessGlyphweaver());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void deadlyVanityKeepsChosenCreatureAndCannotDestroyIndestructibleCreatures() {
        harness.addToBattlefield(player1, new SelflessGlyphweaver());
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new SelflessGlyphweaver());
        Permanent chosenCreature = harness.addToBattlefieldAndReturn(player2, new SelflessGlyphweaver());
        Permanent destroyedCreature = harness.addToBattlefieldAndReturn(player2, new SelflessGlyphweaver());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new KasminaEnigmaSage());
        planeswalker.setCounterCount(CounterType.LOYALTY, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SelflessGlyphweaver()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosenCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(protectedCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(chosenCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(destroyedCreature.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(planeswalker.getCard());
        harness.assertInGraveyard(player1, "Selfless Glyphweaver");
    }

    @Test
    void deadlyVanityResolvesWithoutAChoiceWhenThereAreNoCreaturesOrPlaneswalkers() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new SelflessGlyphweaver()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Selfless Glyphweaver");
    }

    @Test
    void exilesItselfToGiveYourCreaturesIndestructibleUntilEndOfTurn() {
        Permanent glyphweaver = harness.addToBattlefieldAndReturn(player1, new SelflessGlyphweaver());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(glyphweaver.getCard());
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void deadlyVanityKeepsTheChosenCreatureOrPlaneswalker() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent keptPlaneswalker = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        keptPlaneswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new SelflessGlyphweaver()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Exactly 1 permanents");

        harness.handleMultiplePermanentsChosen(player1, List.of(keptPlaneswalker.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Nicol Bolas, Planeswalker");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
    }
}
