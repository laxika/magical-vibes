package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VirtueOfStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HamletGlutton.class, DarksteelRelic.class, GrizzlyBears.class, VirtueOfStrength.class})
class HamletGluttonTest extends BaseCardTest {

    @Test
    void gainsThreeLifeWhenItEnters() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new HamletGlutton()));
        addFullMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    void bargainReducesCostSacrificesArtifactAndStillGainsThreeLife() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new HamletGlutton()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertInGraveyard(player1, "Darksteel Relic");
    }

    @Test
    void cannotBargainBySacrificingCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HamletGlutton()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castKickedCreatureWithPermanent(
                player1, 0, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("an artifact, enchantment, or token");
    }

    @Test
    void canBargainBySacrificingANontokenEnchantment() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new VirtueOfStrength());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new HamletGlutton()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());

        harness.assertInGraveyard(player1, "Virtue of Strength");
        harness.assertNotOnBattlefield(player1, "Virtue of Strength");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hamlet Glutton");
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    void canBargainBySacrificingACreatureToken() {
        HamletGlutton token = new HamletGlutton();
        token.setToken(true);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, token);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new HamletGlutton()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreatureWithPermanent(player1, 0, sacrifice.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(sacrifice.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hamlet Glutton");
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    void cannotCastWithoutBargainUsingOnlyTheReducedManaCost() {
        harness.setHand(player1, List.of(new HamletGlutton()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertNotOnBattlefield(player1, "Hamlet Glutton");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotBargainBySacrificingAnOpponentsArtifact() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        harness.setHand(player1, List.of(new HamletGlutton()));
        addFullMana();

        assertThatThrownBy(() -> harness.castKickedCreatureWithPermanent(
                player1, 0, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Darksteel Relic");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bargainDoesNotReduceTheGreenManaRequirement() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.setHand(player1, List.of(new HamletGlutton()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castKickedCreatureWithPermanent(
                player1, 0, sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Darksteel Relic");
        assertThat(gd.stack).isEmpty();
    }

    private void addFullMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
