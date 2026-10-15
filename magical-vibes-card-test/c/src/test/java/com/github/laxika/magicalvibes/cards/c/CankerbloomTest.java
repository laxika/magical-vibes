package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArmoredScrapgorger;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.s.SkrelvsHive;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cankerbloom.class, ArmoredScrapgorger.class, PropheticPrism.class, SkrelvsHive.class})
class CankerbloomTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact mode sacrifices Cankerbloom and destroys the target artifact")
    void destroysTargetArtifact() {
        addReadyCankerbloom(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cankerbloom");
        harness.assertInGraveyard(player2, "Prophetic Prism");
    }

    @Test
    @DisplayName("Enchantment mode sacrifices Cankerbloom and destroys the target enchantment")
    void destroysTargetEnchantment() {
        addReadyCankerbloom(player1);
        Permanent target = addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cankerbloom");
        harness.assertInGraveyard(player2, "Skrelv's Hive");
    }

    @Test
    @DisplayName("Proliferate mode sacrifices Cankerbloom and adds a counter")
    void proliferates() {
        addReadyCankerbloom(player1);
        Permanent target = addCreatureWithCounter(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        harness.assertInGraveyard(player1, "Cankerbloom");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Artifact mode cannot target an enchantment")
    void artifactModeRejectsEnchantmentTarget() {
        addReadyCankerbloom(player1);
        Permanent target = addReadyEnchantment(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enchantmentModeRejectsArtifactTarget() {
        addReadyCankerbloom(player1);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Cankerbloom");
        harness.assertOnBattlefield(player2, "Prophetic Prism");
    }

    @Test
    void canActivateWhileSummoningSickAndPaysSacrificeImmediately() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Cankerbloom());
        source.setSummoningSick(true);
        Permanent target = addReadyArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Cankerbloom");
        harness.assertOnBattlefield(player2, "Prophetic Prism");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Prophetic Prism");
    }

    @Test
    void cannotActivateWithoutMana() {
        addReadyCankerbloom(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Cankerbloom");
    }

    @Test
    void proliferatesEveryCounterKindAndChosenPlayerOnly() {
        addReadyCankerbloom(player1);
        Permanent chosen = addCreatureWithCounter(player2);
        chosen.setCounterCount(CounterType.OIL, 2);
        Permanent unchosen = addCreatureWithCounter(player1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId(), player2.getId()));

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(chosen.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void proliferateMayChooseNothing() {
        addReadyCankerbloom(player1);
        Permanent target = addCreatureWithCounter(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Cankerbloom");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void proliferateResolvesWithoutAnyCounters() {
        addReadyCankerbloom(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cankerbloom");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyCankerbloom(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new Cankerbloom());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyArtifact(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new PropheticPrism());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyEnchantment(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SkrelvsHive());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addCreatureWithCounter(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ArmoredScrapgorger());
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return permanent;
    }
}
