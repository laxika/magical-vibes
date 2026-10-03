package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GalvanicBlast;
import com.github.laxika.magicalvibes.cards.g.GraspOfDarkness;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarksteelJuggernaut.class, Spellbook.class, GrizzlyBears.class,
        Shatter.class, GalvanicBlast.class, GraspOfDarkness.class})
class DarksteelJuggernautTest extends BaseCardTest {

    @Test
    @DisplayName("P/T is 1/1 when only itself is on the battlefield (it is an artifact)")
    void ptIsOneOneWhenOnlyItself() {
        Permanent juggernaut = addCreatureReady(player1, new DarksteelJuggernaut());

        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T increases with additional artifacts")
    void ptIncreasesWithArtifacts() {
        Permanent juggernaut = addCreatureReady(player1, new DarksteelJuggernaut());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(3);
    }

    @Test
    @DisplayName("P/T only counts controller's artifacts, not opponent's")
    void ptOnlyCountsControllerArtifacts() {
        Permanent juggernaut = addCreatureReady(player1, new DarksteelJuggernaut());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());

        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-artifact creatures do not count toward P/T")
    void nonArtifactCreaturesDoNotCount() {
        Permanent juggernaut = addCreatureReady(player1, new DarksteelJuggernaut());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates as artifacts enter and leave")
    void ptUpdatesAsArtifactsChange() {
        Permanent juggernaut = addCreatureReady(player1, new DarksteelJuggernaut());

        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(1);

        harness.addToBattlefield(player1, new Spellbook());
        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(2);

        harness.addToBattlefield(player1, new Spellbook());
        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Spellbook"));
        assertThat(gqs.getEffectivePower(gd, juggernaut)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, juggernaut)).isEqualTo(1);
    }

    @Test
    void mustAttackEachCombatIfAble() {
        addCreatureReady(player1, new DarksteelJuggernaut());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void doesNotHaveToAttackWhenTapped() {
        Permanent juggernaut = addCreatureReady(player1, new DarksteelJuggernaut());
        juggernaut.tap();

        declareAttackers(List.of());

        assertThat(juggernaut.isAttacking()).isFalse();
    }

    @Test
    void doesNotHaveToAttackWithSummoningSickness() {
        Permanent juggernaut = harness.addToBattlefieldAndReturn(player1, new DarksteelJuggernaut());

        declareAttackers(List.of());

        assertThat(juggernaut.isAttacking()).isFalse();
    }

    @Test
    void dealsDamageEqualToArtifactCountWhenUnblocked() {
        addCreatureReady(player1, new DarksteelJuggernaut());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    void survivesDestroyArtifactSpell() {
        Permanent juggernaut = addCreatureReady(player1, new DarksteelJuggernaut());
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, juggernaut.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darksteel Juggernaut");
        harness.assertNotInGraveyard(player1, "Darksteel Juggernaut");
    }

    @Test
    void survivesLethalDamage() {
        Permanent juggernaut = addCreatureReady(player1, new DarksteelJuggernaut());
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, juggernaut.getId());
        harness.passBothPriorities();

        assertThat(juggernaut.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Darksteel Juggernaut");
        harness.assertNotInGraveyard(player1, "Darksteel Juggernaut");
    }

    @Test
    void indestructibleDoesNotPreventDeathFromZeroOrLessToughness() {
        Permanent juggernaut = addCreatureReady(player1, new DarksteelJuggernaut());
        harness.setHand(player1, List.of(new GraspOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, juggernaut.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Darksteel Juggernaut");
        harness.assertInGraveyard(player1, "Darksteel Juggernaut");
    }

    @Test
    void characteristicPowerAndToughnessWorkInHandAndGraveyard() {
        DarksteelJuggernaut inHand = new DarksteelJuggernaut();
        DarksteelJuggernaut inGraveyard = new DarksteelJuggernaut();
        harness.setHand(player1, List.of(inHand));
        harness.setGraveyard(player1, List.of(inGraveyard));
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new Spellbook());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).clear();

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, inHand)).isZero();
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, inGraveyard)).isZero();
    }
}
