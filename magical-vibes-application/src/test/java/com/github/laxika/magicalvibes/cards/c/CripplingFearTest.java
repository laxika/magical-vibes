package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mistwalker;
import com.github.laxika.magicalvibes.cards.r.RavenousLindwurm;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CripplingFear.class, AvatarOfMight.class, AvianChangeling.class, GrizzlyBears.class,
        Mistwalker.class, RavenousLindwurm.class})
class CripplingFearTest extends BaseCardTest {

    @Test
    @DisplayName("Weakens creatures that are not of the chosen type on every battlefield")
    void weakensCreaturesNotOfChosenType() {
        Permanent ownBear = addReadyCreature(player1, new GrizzlyBears());
        Permanent opposingBear = addReadyCreature(player2, new GrizzlyBears());
        Permanent ownAvatar = addReadyCreature(player1, new AvatarOfMight());
        Permanent opposingAvatar = addReadyCreature(player2, new AvatarOfMight());

        castCripplingFear(player1);
        harness.handleListChoice(player1, "BEAR");

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownAvatar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownAvatar)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opposingAvatar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opposingAvatar)).isEqualTo(5);
    }

    @Test
    @DisplayName("A Changeling counts as the chosen type")
    void changelingCountsAsChosenType() {
        Permanent changeling = addReadyCreature(player2, new AvianChangeling());
        Permanent bear = addReadyCreature(player2, new GrizzlyBears());

        castCripplingFear(player1);
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(changeling).doesNotContain(bear);
    }

    @Test
    @DisplayName("The -3/-3 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent avatar = addReadyCreature(player2, new AvatarOfMight());

        castCripplingFear(player1);
        harness.handleListChoice(player1, "BEAR");

        assertThat(avatar.getPowerModifier()).isEqualTo(-3);
        assertThat(avatar.getToughnessModifier()).isEqualTo(-3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(avatar.getPowerModifier()).isEqualTo(0);
        assertThat(avatar.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Creatures entering after resolution are not weakened")
    void doesNotAffectLaterCreatures() {
        Permanent existing = harness.addToBattlefieldAndReturn(player2, new RavenousLindwurm());

        castCripplingFear(player1);
        harness.handleListChoice(player1, "GOBLIN");
        Permanent later = harness.addToBattlefieldAndReturn(player2, new RavenousLindwurm());

        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, existing)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(6);
    }

    @Test
    @DisplayName("Each cast makes a fresh type choice and preserves changelings")
    void repeatedCastsChooseIndependently() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new RavenousLindwurm());
        Permanent changeling = harness.addToBattlefieldAndReturn(player2, new Mistwalker());

        castCripplingFear(player1);
        harness.handleListChoice(player1, "WURM");
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(6);

        castCripplingFear(player1);
        harness.handleListChoice(player1, "GOBLIN");
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(4);

        castCripplingFear(player1);
        harness.handleListChoice(player1, "GOBLIN");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(changeling).doesNotContain(wurm);
        harness.assertInGraveyard(player2, "Ravenous Lindwurm");
    }

    @Test
    @DisplayName("Can resolve on an empty battlefield and does not affect later creatures")
    void resolvesOnEmptyBattlefield() {
        castCripplingFear(player1);
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Crippling Fear");
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new RavenousLindwurm());
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(6);
    }

    private void castCripplingFear(Player player) {
        harness.castFromHand(player, new CripplingFear(), "{2}{B}{B}");
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
