package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SummonAlexander;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrystalFragments.class, SummonAlexander.class, GrizzlyBears.class, Shock.class})
class CrystalFragmentsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent crystal = addCrystalReady(player1);
        crystal.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exile-and-return activation transforms Crystal Fragments into Summon Alexander")
    void transformsIntoSummonAlexander() {
        Permanent crystal = addCrystalReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, indexOf(player1, crystal), 0, null, null);
        harness.passBothPriorities();

        Permanent transformed = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof SummonAlexander)
                .findFirst()
                .orElse(null);
        assertThat(transformed).isNotNull();
        assertThat(transformed.isTransformed()).isTrue();
        assertThat(transformed.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Transform returns Crystal Fragments under its owner's control")
    void transformReturnsUnderOwnersControl() {
        CrystalFragments card = new CrystalFragments();
        card.setOwnerId(player2.getId());
        Permanent crystal = new Permanent(card);
        crystal.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(crystal);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, indexOf(player1, crystal), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof SummonAlexander);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard() instanceof SummonAlexander);
    }

    @Test
    @DisplayName("Transform activation is restricted to sorcery speed")
    void transformIsSorcerySpeedOnly() {
        Permanent crystal = addCrystalReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, crystal), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Chapter I prevents all damage to creatures you control until end of turn")
    void chapterIPreventsDamageToControlledCreatures() {
        Permanent alexander = addTransformedAlexander(player1, 0);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        advanceToPrecombatMain();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.getMarkedDamage()).isZero();

        harness.forceStep(TurnStep.CLEANUP);
        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(alexander.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter III taps all opponent creatures and leaves your creatures untapped")
    void chapterIIITapsOpponentsCreatures() {
        addTransformedAlexander(player1, 2);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToPrecombatMain();
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isTrue();
    }

    private void advanceToPrecombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addCrystalReady(Player player) {
        Permanent crystal = new Permanent(new CrystalFragments());
        crystal.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(crystal);
        return crystal;
    }

    private Permanent addTransformedAlexander(Player player, int loreCounters) {
        CrystalFragments crystal = new CrystalFragments();
        Permanent alexander = new Permanent(crystal);
        alexander.setCard(crystal.getBackFaceCard());
        alexander.setTransformed(true);
        alexander.setSummoningSick(false);
        alexander.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player.getId()).add(alexander);
        return alexander;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
