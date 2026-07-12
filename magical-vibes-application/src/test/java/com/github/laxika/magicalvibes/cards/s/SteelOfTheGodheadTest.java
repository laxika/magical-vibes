package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SteelOfTheGodheadTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Resolving Steel of the Godhead attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent target = new Permanent(new EliteVanguard());
        target.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new SteelOfTheGodhead()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Steel of the Godhead")
                        && p.isAttached()
                        && p.getAttachedTo().equals(target.getId()));
    }

    // ===== White enchanted creature: +1/+1 and lifelink =====

    @Test
    @DisplayName("White enchanted creature gets +1/+1 and lifelink, but is not unblockable")
    void whiteCreatureGetsBoostAndLifelink() {
        Permanent white = new Permanent(new EliteVanguard()); // 2/1 white
        gd.playerBattlefields.get(player1.getId()).add(white);

        Permanent steel = new Permanent(new SteelOfTheGodhead());
        steel.setAttachedTo(white.getId());
        gd.playerBattlefields.get(player1.getId()).add(steel);

        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, white)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, white, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, white)).isFalse();
    }

    // ===== Blue enchanted creature: +1/+1 and can't be blocked =====

    @Test
    @DisplayName("Blue enchanted creature gets +1/+1 and can't be blocked, but no lifelink")
    void blueCreatureGetsBoostAndUnblockable() {
        Permanent blue = new Permanent(new FugitiveWizard()); // 1/1 blue
        gd.playerBattlefields.get(player1.getId()).add(blue);

        Permanent steel = new Permanent(new SteelOfTheGodhead());
        steel.setAttachedTo(blue.getId());
        gd.playerBattlefields.get(player1.getId()).add(steel);

        assertThat(gqs.getEffectivePower(gd, blue)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blue)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, blue)).isTrue();
        assertThat(gqs.hasKeyword(gd, blue, Keyword.LIFELINK)).isFalse();
    }

    // ===== Neither white nor blue: no bonuses =====

    @Test
    @DisplayName("A creature that is neither white nor blue gets no bonuses")
    void nonWhiteNonBlueGetsNothing() {
        Permanent green = new Permanent(new GrizzlyBears()); // 2/2 green
        gd.playerBattlefields.get(player1.getId()).add(green);

        Permanent steel = new Permanent(new SteelOfTheGodhead());
        steel.setAttachedTo(green.getId());
        gd.playerBattlefields.get(player1.getId()).add(steel);

        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, green, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, green)).isFalse();
    }

    // ===== Bonuses fall off when the aura leaves =====

    @Test
    @DisplayName("Bonuses are removed when Steel of the Godhead leaves the battlefield")
    void bonusesRemovedWhenAuraRemoved() {
        Permanent white = new Permanent(new EliteVanguard()); // 2/1 white
        gd.playerBattlefields.get(player1.getId()).add(white);

        Permanent steel = new Permanent(new SteelOfTheGodhead());
        steel.setAttachedTo(white.getId());
        gd.playerBattlefields.get(player1.getId()).add(steel);

        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, white, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(steel);

        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, white)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, white, Keyword.LIFELINK)).isFalse();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Steel of the Godhead")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SteelOfTheGodhead()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
