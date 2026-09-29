package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KlauthsWill.class, EdgarMarkov.class, GloriousAnthem.class, GrizzlyBears.class, Ornithopter.class})
class KlauthsWillTest extends BaseCardTest {

    @Test
    void breatheFlameDamagesOnlyCreaturesWithoutFlying() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Ornithopter());

        cast(ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0}), 2, List.of());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    @Test
    void smashRelicsDestroysUpToXTargets() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addToBattlefield(player2, new GloriousAnthem());

        cast(ChooseOneEffect.encodeModeSelection(1, 2, new int[]{1}), 2, List.of(artifact.getId()));

        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    void smashRelicsRejectsNonartifactNonenchantmentTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KlauthsWill()));
        addMana(1);

        assertThatThrownBy(() -> gs.playModalXCard(
                gd, player1, 0, ChooseOneEffect.encodeModeSelection(1, 2, new int[]{1}),
                1, null, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void commanderAllowsBothModes() {
        addToCommandZone(player1, new EdgarMarkov());
        addCreatureReady(player1, new EdgarMarkov());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        cast(ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0, 1}), 2, List.of(artifact.getId()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    void bothModesRequireControllingCommanderAsCast() {
        harness.setHand(player1, List.of(new KlauthsWill()));
        addMana(2);

        assertThatThrownBy(() -> gs.playModalXCard(
                gd, player1, 0, ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0, 1}), 2,
                null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int modeSelection, int xValue, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new KlauthsWill()));
        addMana(xValue);
        gs.playModalXCard(gd, player1, 0, modeSelection, xValue, null, targetIds);
        harness.passBothPriorities();
    }

    private void addMana(int xValue) {
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void addToCommandZone(Player player, Card card) {
        gd.playerCommandZones.get(player.getId()).add(card);
    }
}
