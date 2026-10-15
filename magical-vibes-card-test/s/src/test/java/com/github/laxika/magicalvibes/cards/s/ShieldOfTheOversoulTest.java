package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RhysTheRedeemed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShieldOfTheOversoul.class, GrizzlyBears.class, EliteVanguard.class, FugitiveWizard.class, FountainOfYouth.class, RhysTheRedeemed.class, Scuttlemutt.class})
class ShieldOfTheOversoulTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Shield of the Oversoul attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ShieldOfTheOversoul()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Shield of the Oversoul")
                        && p.isAttached()
                        && p.getAttachedTo().equals(target.getId()));
    }

    @Test
    @DisplayName("Green enchanted creature gets +1/+1 and indestructible, but no flying")
    void greenCreatureGetsBoostAndIndestructible() {
        Permanent green = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // 2/2 green

        Permanent shield = harness.addToBattlefieldAndReturn(player1, new ShieldOfTheOversoul());
        shield.setAttachedTo(green.getId());

        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, green, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, green, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("White enchanted creature gets +1/+1 and flying, but is not indestructible")
    void whiteCreatureGetsBoostAndFlying() {
        Permanent white = harness.addToBattlefieldAndReturn(player1, new EliteVanguard()); // 2/1 white

        Permanent shield = harness.addToBattlefieldAndReturn(player1, new ShieldOfTheOversoul());
        shield.setAttachedTo(white.getId());

        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, white)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, white, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, white, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("A green-and-white enchanted creature gets +2/+2, indestructible, and flying")
    void greenWhiteCreatureGetsBothBonuses() {
        Permanent gw = harness.addToBattlefieldAndReturn(player1, new RhysTheRedeemed()); // 1/1 green/white

        Permanent shield = harness.addToBattlefieldAndReturn(player1, new ShieldOfTheOversoul());
        shield.setAttachedTo(gw.getId());

        assertThat(gqs.getEffectivePower(gd, gw)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gw)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, gw, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, gw, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A creature that is neither green nor white gets no bonuses")
    void nonGreenNonWhiteGetsNothing() {
        Permanent blue = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard()); // 1/1 blue

        Permanent shield = harness.addToBattlefieldAndReturn(player1, new ShieldOfTheOversoul());
        shield.setAttachedTo(blue.getId());

        assertThat(gqs.getEffectivePower(gd, blue)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blue)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, blue, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, blue, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Bonuses are removed when Shield of the Oversoul leaves the battlefield")
    void bonusesRemovedWhenAuraRemoved() {
        Permanent green = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // 2/2 green

        Permanent shield = harness.addToBattlefieldAndReturn(player1, new ShieldOfTheOversoul());
        shield.setAttachedTo(green.getId());

        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, green, Keyword.INDESTRUCTIBLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(shield);

        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, green, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @CardUsed({ShieldOfTheOversoul.class, RhysTheRedeemed.class, Scuttlemutt.class})
    @DisplayName("Bonuses follow the enchanted opponent creature's current colors")
    void bonusesFollowColorChanges() {
        Permanent mutt = addCreatureReady(player1, new Scuttlemutt());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RhysTheRedeemed());
        harness.setHand(player1, List.of(new ShieldOfTheOversoul()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "DONE");

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(mutt.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Shield of the Oversoul")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ShieldOfTheOversoul()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
