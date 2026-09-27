package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IndomitableWill.class, IsamaruHoundOfKonda.class, LanternKami.class, Forest.class})
class IndomitableWillTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Indomitable Will attaches it and boosts the enchanted creature")
    void resolvingAttachesAndBoostsCreature() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new IndomitableWill()));
        addCastingMana();

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Indomitable Will");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Indomitable Will boosts only the enchanted creature")
    void onlyEnchantedCreatureGetsBoost() {
        Permanent enchanted = addCreatureReady(player1, new LanternKami());
        Permanent other = addCreatureReady(player1, new LanternKami());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IndomitableWill());
        aura.setAttachedTo(enchanted.getId());

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
    }

    @Test
    @DisplayName("The creature loses Indomitable Will's boost when the Aura leaves")
    void boostStopsWhenAuraIsRemoved() {
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IndomitableWill());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Indomitable Will can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new IndomitableWill()));
        addCastingMana();

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Indomitable Will");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Indomitable Will can be cast during an opponent's turn because it has flash")
    void canBeCastDuringOpponentsTurnWithFlash() {
        Permanent creature = addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new IndomitableWill()));
        addCastingMana();

        harness.getGameService().passPriority(harness.getGameData(), player2);
        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Indomitable Will goes to its owner's graveyard if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new IndomitableWill()));
        addCastingMana();

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Indomitable Will");
        harness.assertNotOnBattlefield(player1, "Indomitable Will");
    }

    @Test
    @DisplayName("Indomitable Will cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new IndomitableWill()));
        addCastingMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
