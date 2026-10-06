package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BraveKinDuo;
import com.github.laxika.magicalvibes.cards.c.CoruscationMage;
import com.github.laxika.magicalvibes.cards.d.DaggerfangDuo;
import com.github.laxika.magicalvibes.cards.f.FrilledSparkshooter;
import com.github.laxika.magicalvibes.cards.t.TakeOutTheTrash;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RockfaceVillage.class, FrilledSparkshooter.class, DaggerfangDuo.class,
        BraveKinDuo.class, CoruscationMage.class, RaccoonRallier.class, TakeOutTheTrash.class})
class RockfaceVillageTest extends BaseCardTest {

    @Test
    void tapsForColorless() {
        addVillage();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void addsRedManaOnlyForCreatureSpells() {
        addVillage();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.RED))
                .isEqualTo(1);
    }

    @Test
    void creatureOnlyRedManaCannotCastNoncreatureSpells() {
        addVillage();
        harness.activateAbility(player1, 0, 1, null, null);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FrilledSparkshooter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new TakeOutTheTrash()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void boostsQualifyingCreatureAndGrantsHasteUntilEndOfTurn() {
        addVillage();
        Permanent lizard = harness.addToBattlefieldAndReturn(player1, new FrilledSparkshooter());
        int originalPower = lizard.getEffectivePower();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, null, lizard.getId());
        harness.passBothPriorities();

        assertThat(lizard.getEffectivePower()).isEqualTo(originalPower + 1);
        assertThat(gqs.hasKeyword(gd, lizard, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(lizard.getEffectivePower()).isEqualTo(originalPower);
        assertThat(gqs.hasKeyword(gd, lizard, Keyword.HASTE)).isFalse();
    }

    @Test
    void cannotTargetNonKindredCreature() {
        addVillage();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DaggerfangDuo());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateBoostAbilityOutsideSorcerySpeed() {
        addVillage();
        Permanent lizard = harness.addToBattlefieldAndReturn(player1, new FrilledSparkshooter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, lizard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureOnlyRedManaCanPayForCreatureSpell() {
        addVillage();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new RaccoonRallier()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raccoon Rallier");
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.RED)).isZero();
    }

    @Test
    void unrestrictedRedManaCanCastSameNoncreatureSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FrilledSparkshooter());
        harness.setHand(player1, List.of(new TakeOutTheTrash()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Frilled Sparkshooter");
    }

    @Test
    void creatureOnlyRedManaCannotPayForBoostAbility() {
        addVillage();
        harness.addToBattlefield(player1, new RockfaceVillage());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FrilledSparkshooter());
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isTapped()).isFalse();
    }

    @Test
    void boostsMouseOtterAndRaccoon() {
        List<Card> creatures = List.of(
                new BraveKinDuo(), new CoruscationMage(), new RaccoonRallier());
        for (var card : creatures) {
            int villageIndex = gd.playerBattlefields.get(player1.getId()).size();
            addVillage();
            Permanent target = harness.addToBattlefieldAndReturn(player1, card);
            int power = target.getEffectivePower();
            int toughness = target.getEffectiveToughness();
            harness.addMana(player1, ManaColor.RED, 1);

            harness.activateAbility(player1, villageIndex, 2, null, target.getId());
            harness.passBothPriorities();

            assertThat(target.getEffectivePower()).isEqualTo(power + 1);
            assertThat(target.getEffectiveToughness()).isEqualTo(toughness);
            assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
            assertThat(gd.playerBattlefields.get(player1.getId()).get(villageIndex).isTapped()).isTrue();
        }
    }

    @Test
    void cannotTargetOpponentsQualifyingCreature() {
        addVillage();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FrilledSparkshooter());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateBoostDuringOpponentsMainPhase() {
        addVillage();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FrilledSparkshooter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateBoostWithSpellOnStack() {
        addVillage();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FrilledSparkshooter());
        harness.setHand(player1, List.of(new RaccoonRallier()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void manaAbilitiesResolveImmediatelyAndTapVillage() {
        addVillage();
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostStillResolvesAfterVillageLeavesBattlefield() {
        addVillage();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FrilledSparkshooter());
        int power = target.getEffectivePower();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 2, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).removeFirst();

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(power + 1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    void creatureOnlyRedManaCanPayGenericCreatureCost() {
        addVillage();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new DaggerfangDuo()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertOnBattlefield(player1, "Daggerfang Duo");
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.RED)).isZero();
    }

    @Test
    void boostDoesNotResolveIfTargetChangesController() {
        addVillage();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FrilledSparkshooter());
        int power = target.getEffectivePower();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 2, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(power);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void addVillage() {
        harness.addToBattlefield(player1, new RockfaceVillage());
    }
}
