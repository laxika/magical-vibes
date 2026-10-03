package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GryffsBoon;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DescendUponTheSinful.class, DevilthornFox.class, Forest.class,
        FieryTemper.class, MagnifyingGlass.class, GryffsBoon.class})
class DescendUponTheSinfulTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all creatures and does not create an Angel without delirium")
    void exilesAllCreaturesWithoutDelirium() {
        harness.addToBattlefield(player1, new DevilthornFox());
        harness.addToBattlefield(player2, new DevilthornFox());
        harness.addToBattlefield(player1, new Forest());
        cast();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Devilthorn Fox");
        harness.assertNotOnBattlefield(player2, "Devilthorn Fox");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Devilthorn Fox"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("With delirium, creates a 4/4 flying Angel after exiling all creatures")
    void withDeliriumCreatesAngel() {
        harness.setGraveyard(player1, List.of(
                new DevilthornFox(), new Forest(), new FieryTemper(), new MagnifyingGlass()));
        harness.addToBattlefield(player1, new DevilthornFox());
        harness.addToBattlefield(player2, new DevilthornFox());
        cast();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Devilthorn Fox");
        harness.assertNotOnBattlefield(player2, "Devilthorn Fox");
        List<Permanent> angels = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Angel"))
                .toList();
        assertThat(angels).singleElement().satisfies(angel -> {
            assertThat(angel.getCard().getPower()).isEqualTo(4);
            assertThat(angel.getCard().getToughness()).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("The resolving spell does not count as a fourth card type")
    void resolvingSpellDoesNotEnableDelirium() {
        harness.setGraveyard(player1, List.of(
                new DevilthornFox(), new Forest(), new MagnifyingGlass()));
        cast();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angel");
        harness.assertInGraveyard(player1, "Descend upon the Sinful");
    }

    @Test
    @DisplayName("Four cards of only three types do not enable delirium")
    void duplicateTypesDoNotEnableDelirium() {
        harness.setGraveyard(player1, List.of(
                new DevilthornFox(), new DevilthornFox(), new Forest(), new MagnifyingGlass()));
        cast();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angel");
    }

    @Test
    @DisplayName("An opponent's delirium does not create an Angel for the caster")
    void opponentsGraveyardDoesNotEnableDelirium() {
        harness.setGraveyard(player2, List.of(
                new DevilthornFox(), new Forest(), new FieryTemper(), new MagnifyingGlass()));
        cast();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angel");
        harness.assertNotOnBattlefield(player2, "Angel");
    }

    @Test
    @DisplayName("Delirium gained before resolution creates an Angel even with no creatures to exile")
    void deliriumIsCheckedAtResolution() {
        harness.setGraveyard(player1, List.of(
                new DevilthornFox(), new Forest(), new MagnifyingGlass()));
        cast();
        harness.setGraveyard(player1, List.of(
                new DevilthornFox(), new Forest(), new MagnifyingGlass(), new FieryTemper()));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Angel");
        harness.assertNotOnBattlefield(player2, "Angel");
    }

    @Test
    @DisplayName("Delirium lost before resolution does not create an Angel")
    void deliriumLostBeforeResolution() {
        harness.setGraveyard(player1, List.of(
                new DevilthornFox(), new Forest(), new MagnifyingGlass(), new FieryTemper()));
        cast();
        harness.setGraveyard(player1, List.of(
                new DevilthornFox(), new Forest(), new MagnifyingGlass()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Angel");
    }

    @Test
    @DisplayName("An Aura on an exiled creature reaches the graveyard too late to enable delirium")
    void orphanedAuraDoesNotEnableDeliriumDuringResolution() {
        harness.setGraveyard(player1, List.of(
                new DevilthornFox(), new Forest(), new MagnifyingGlass()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GryffsBoon());
        aura.setAttachedTo(creature.getId());
        cast();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Devilthorn Fox");
        harness.assertInGraveyard(player1, "Gryff's Boon");
        harness.assertNotOnBattlefield(player1, "Angel");
    }

    private void cast() {
        harness.castFromHand(player1, new DescendUponTheSinful(), "{4}{W}{W}");
    }
}
