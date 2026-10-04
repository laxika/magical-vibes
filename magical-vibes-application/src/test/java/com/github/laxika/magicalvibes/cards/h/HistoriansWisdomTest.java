package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BoonOfBoseiju;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreaterTanuki;
import com.github.laxika.magicalvibes.cards.j.JukaiTrainee;
import com.github.laxika.magicalvibes.cards.n.NetworkTerminal;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HistoriansWisdom.class, BoonOfBoseiju.class, GreaterTanuki.class, NetworkTerminal.class, Forest.class, JukaiTrainee.class})
class HistoriansWisdomTest extends BaseCardTest {

    @Test
    void drawsAndBoostsWhenEnchantedCreatureHasGreatestPower() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        harness.setLibrary(player1, List.of(new Forest()));
        castOn(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void onlyBoostsWhenAnotherCreatureHasGreaterPower() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        harness.addToBattlefield(player2, new GreaterTanuki());
        harness.setLibrary(player1, List.of(new Forest()));
        castOn(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    void canEnchantNoncreatureArtifactWithoutDrawingOrBoosting() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        harness.setLibrary(player1, List.of(new Forest()));
        castOn(artifact);

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    void cannotEnchantLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new HistoriansWisdom()));
        addMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    void drawsWhenAuraBoostCreatesTieForGreatestPower() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new JukaiTrainee());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, List.of(new Forest()));

        castOn(creature);

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void drawsForAuraControllerWhenEnchantingOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new JukaiTrainee());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        castOn(creature);

        harness.assertInHand(player1, "Forest");
        harness.assertNotInHand(player2, "Forest");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    void doesNotTriggerWhenCreatureLacksGreatestPowerAtEntry() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        harness.addToBattlefield(player2, new GreaterTanuki());
        harness.setLibrary(player1, List.of(new Forest()));

        castAura(creature);

        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    void laterPowerIncreaseCannotCreateDrawTrigger() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        harness.addToBattlefield(player2, new GreaterTanuki());
        harness.setLibrary(player1, List.of(new Forest()));
        castAura(creature);

        harness.setHand(player1, List.of(new BoonOfBoseiju()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    void rechecksGreatestPowerWhenDrawTriggerResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new JukaiTrainee());
        harness.setLibrary(player1, List.of(new Forest()));
        castAura(creature);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new BoonOfBoseiju()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, opponent.getId());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Forest");
    }

    private void castOn(Permanent target) {
        castAura(target);
        harness.passBothPriorities();
    }

    private void castAura(Permanent target) {
        harness.setHand(player1, List.of(new HistoriansWisdom()));
        addMana();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
