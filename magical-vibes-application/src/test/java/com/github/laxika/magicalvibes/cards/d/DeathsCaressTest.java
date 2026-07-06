package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureAndGainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeathsCaressTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Death's Caress targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent target = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Death's Caress");
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Destroying a Human gains life equal to its toughness")
    void destroyingHumanGainsLife() {
        Permanent human = new Permanent(new EliteVanguard()); // 2/1 Human Soldier
        harness.getGameData().playerBattlefields.get(player2.getId()).add(human);

        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, human.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elite Vanguard"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Elite Vanguard"));
        // Elite Vanguard has toughness 1, so controller gains 1 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Destroying a non-Human creature grants no life")
    void destroyingNonHumanGrantsNoLife() {
        Permanent nonHuman = new Permanent(new GrizzlyBears()); // 2/2 Bear
        harness.getGameData().playerBattlefields.get(player2.getId()).add(nonHuman);

        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, nonHuman.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Grizzly Bears is not a Human, so no life is gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Death's Caress goes to the graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent target = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Death's Caress"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent human = new Permanent(new EliteVanguard());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(human);

        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, human.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // No life is gained when the spell fizzles
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        // Death's Caress still goes to the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Death's Caress"));
    }
}
