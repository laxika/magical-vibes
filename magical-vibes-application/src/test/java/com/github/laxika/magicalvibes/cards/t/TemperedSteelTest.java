package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.o.OriginSpellbomb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemperedSteel.class, GoldMyr.class, CarapaceForger.class, OriginSpellbomb.class, Memnite.class, LiquimetalCoating.class, Opalescence.class})
class TemperedSteelTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new TemperedSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving puts Tempered Steel onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new TemperedSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tempered Steel");
    }

    @Test
    @DisplayName("Own artifact creatures get +2/+2")
    void buffsOwnArtifactCreatures() {
        harness.addToBattlefield(player1, new TemperedSteel());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new GoldMyr());

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(3);   // 1 base + 2
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(3); // 1 base + 2
    }

    @Test
    @DisplayName("Does not buff non-artifact creatures")
    void doesNotBuffNonArtifactCreatures() {
        harness.addToBattlefield(player1, new TemperedSteel());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-creature artifacts")
    void doesNotBuffNonCreatureArtifacts() {
        harness.addToBattlefield(player1, new TemperedSteel());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new OriginSpellbomb());

        // OriginSpellbomb is a non-creature artifact, should not be affected
        assertThat(gqs.getEffectivePower(gd, spellbook)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, spellbook)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not buff opponent's artifact creatures")
    void doesNotBuffOpponentArtifactCreatures() {
        harness.addToBattlefield(player1, new TemperedSteel());
        Permanent opponentMyr = harness.addToBattlefieldAndReturn(player2, new GoldMyr());

        assertThat(gqs.getEffectivePower(gd, opponentMyr)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentMyr)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Tempered Steels give +4/+4 to artifact creatures")
    void twoTemperedSteelsStack() {
        harness.addToBattlefield(player1, new TemperedSteel());
        harness.addToBattlefield(player1, new TemperedSteel());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new GoldMyr());

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(5);   // 1 base + 4
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(5); // 1 base + 4
    }

    @Test
    @DisplayName("Bonus is removed when Tempered Steel leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new TemperedSteel());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new GoldMyr());

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Tempered Steel"));

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bonus applies when Tempered Steel resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new GoldMyr());
        harness.setHand(player1, List.of(new TemperedSteel()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent myr = findPermanent(player1, "Gold Myr");

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(3);
    }
    @Test
    @DisplayName("Artifact creatures entering after Tempered Steel resolves receive the bonus")
    void buffsArtifactCreatureCastAfterSourceResolves() {
        harness.setHand(player1, List.of(new TemperedSteel(), new Memnite()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent memnite = findPermanent(player1, "Memnite");
        assertThat(gqs.getEffectivePower(gd, memnite)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, memnite)).isEqualTo(3);
    }

    @Test
    @DisplayName("Tempered Steel boosts itself when it becomes an artifact creature")
    void buffsItselfWhenItIsAnArtifactCreature() {
        Permanent steel = harness.addToBattlefieldAndReturn(player1, new TemperedSteel());
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new LiquimetalCoating());

        assertThat(gqs.getEffectivePower(gd, steel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, steel)).isEqualTo(3);

        harness.activateAbility(player1, 2, null, steel.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, steel)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, steel)).isEqualTo(5);
    }
}
