package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KlauthsWill.class, EdgarMarkov.class, GloriousAnthem.class, GrizzlyBears.class, Ornithopter.class})
class KlauthsWillTest extends BaseCardTest {

    @Test
    void breatheFlameDamagesOnlyCreaturesWithoutFlying() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Ornithopter());

        cast(new int[]{0}, 2, List.of());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    @Test
    void smashRelicsDestroysUpToXTargets() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.addToBattlefield(player2, new GloriousAnthem());

        cast(new int[]{1}, 2, List.of(artifact.getId()));

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
        addCommander();
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        cast(new int[]{0, 1}, 2, List.of(artifact.getId()));

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

    @Test
    void breatheFlameAlsoDamagesOwnCreaturesButNotPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(new int[]{0}, 2, List.of());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void smashRelicsDestroysArtifactsAndEnchantmentsAcrossBothControllers() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(new int[]{1}, 2,
                List.of(artifact.getId(), enchantment.getId()));

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    void smashRelicsAllowsZeroTargetsWithZeroX() {
        harness.addToBattlefield(player2, new Ornithopter());

        cast(new int[]{1}, 0, List.of());

        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player1, "Klauth's Will");
    }

    @Test
    void smashRelicsRejectsMoreTargetsThanX() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new KlauthsWill()));
        addMana(1);

        assertThatThrownBy(() -> gs.playModalXCard(gd, player1, 0,
                ChooseOneEffect.encodeModeSelection(1, 2, new int[]{1}), 1, null,
                List.of(artifact.getId(), enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void commanderInCommandZoneDoesNotAllowBothModes() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        gd.playerCommandZones.get(player1.getId()).add(commander);
        harness.setHand(player1, List.of(new KlauthsWill()));
        addMana(2);

        assertThatThrownBy(() -> gs.playModalXCard(gd, player1, 0,
                ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0, 1}), 2, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesStillResolveAfterCommanderLeavesBattlefield() {
        Permanent commander = addCommander();
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new KlauthsWill()));
        addMana(2);
        gs.playModalXCard(gd, player1, 0,
                ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0, 1}), 2, null,
                List.of(artifact.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(commander);
        gd.playerGraveyards.get(player1.getId()).add(commander.getOriginalCard());

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    void bothModesWithNoTargetsStillDealDamage() {
        addCommander();
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(new int[]{0, 1}, 2, List.of());

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void bothModesDoNotDealDamageWhenAllChosenTargetsBecomeIllegal() {
        addCommander();
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new KlauthsWill()));
        addMana(2);
        gs.playModalXCard(gd, player1, 0,
                ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0, 1}), 2, null,
                List.of(artifact.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        gd.playerGraveyards.get(player2.getId()).add(artifact.getOriginalCard());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Klauth's Will");
    }

    @Test
    void smashRelicsCanDestroyMoreThanOneHundredTargets() {
        List<UUID> targets = IntStream.range(0, 101)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new Ornithopter()).getId())
                .toList();

        cast(new int[]{1}, 101, targets);

        harness.assertNotOnBattlefield(player2, "Ornithopter");
    }

    private void cast(int[] modes, int xValue, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new KlauthsWill()));
        addMana(xValue);
        harness.castModalSorceryWithModesForX(player1, 0, 1, 2, modes, xValue, targetIds);
        harness.passBothPriorities();
    }

    private void addMana(int xValue) {
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private Permanent addCommander() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        return harness.addToBattlefieldAndReturn(player1, commander);
    }
}
