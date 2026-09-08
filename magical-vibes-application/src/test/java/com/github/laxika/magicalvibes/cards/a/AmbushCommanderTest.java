package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmbushCommander.class, Forest.class, Mountain.class, GrizzlyBears.class})
class AmbushCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("Forests you control become 1/1 green Elf creatures that are still lands")
    void animatesControlledForests() {
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AmbushCommander());

        assertThat(gqs.isCreature(gd, ownForest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownForest)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownForest)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, ownForest)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasEffectiveSubtype(gd, ownForest, CardSubtype.ELF)).isTrue();
        assertThat(gqs.isLand(gd, ownForest)).isTrue();

        assertThat(gqs.isCreature(gd, opponentForest)).isFalse();
        assertThat(gqs.isCreature(gd, mountain)).isFalse();
        assertThat(gqs.isCreature(gd, bears)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing an Elf gives a target creature +3/+3 until end of turn")
    void sacrificesElfToBoostTarget() {
        harness.addToBattlefield(player1, new AmbushCommander());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("The commander itself can be sacrificed as the Elf cost")
    void canSacrificeItself() {
        harness.addToBattlefield(player1, new AmbushCommander());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ambush Commander");
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }
}
