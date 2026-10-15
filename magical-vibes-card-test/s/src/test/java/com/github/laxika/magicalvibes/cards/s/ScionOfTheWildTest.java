package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScionOfTheWild.class, GrizzlyBears.class, GloriousAnthem.class})
class ScionOfTheWildTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Scion of the Wild puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ScionOfTheWild()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Scion of the Wild");
    }

    @Test
    @DisplayName("Scion of the Wild is 1/1 when it is your only creature")
    void isOneOneWhenOnlyCreature() {
        Permanent scion = addCreatureReady(player1, new ScionOfTheWild());

        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(1);
    }

    @Test
    @DisplayName("Scion of the Wild power and toughness equal creatures you control")
    void ptEqualsControlledCreatures() {
        Permanent scion = addCreatureReady(player1, new ScionOfTheWild());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(3);
    }

    @Test
    @DisplayName("Scion of the Wild counts only your creatures, not opponent creatures")
    void countsOnlyControllersCreatures() {
        Permanent scion = addCreatureReady(player1, new ScionOfTheWild());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(1);
    }

    @Test
    @DisplayName("Scion of the Wild power and toughness update as creatures enter and leave")
    void ptUpdatesAsCreaturesChange() {
        Permanent scion = addCreatureReady(player1, new ScionOfTheWild());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(2);

        harness.addToBattlefield(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(1);
    }

    @Test
    @DisplayName("Scion of the Wild characteristic-defining P/T stacks with static bonuses")
    void ptStacksWithStaticBonuses() {
        Permanent scion = addCreatureReady(player1, new ScionOfTheWild());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving Scion counts itself and existing creatures but not enchantments")
    void resolvingCountsOnlyCreaturesIncludingItself() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.setHand(player1, List.of(new ScionOfTheWild()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent scion = findPermanent(player1, "Scion of the Wild");
        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(3);
    }

    @Test
    @DisplayName("Scion in hand is 0/0 without controlled creatures and updates as they enter")
    void characteristicAbilityWorksInHand() {
        ScionOfTheWild scion = new ScionOfTheWild();
        harness.setHand(player1, List.of(scion));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertThat(gqs.getEffectiveCardPower(gd, scion)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, scion)).isZero();

        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectiveCardPower(gd, scion)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, scion)).isEqualTo(1);
    }

    @Test
    @DisplayName("Scion in a graveyard counts its owner's creatures without battlefield bonuses")
    void characteristicAbilityWorksInGraveyard() {
        ScionOfTheWild scion = new ScionOfTheWild();
        harness.setGraveyard(player1, List.of(scion, new GrizzlyBears()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectiveCardPower(gd, scion)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, scion)).isEqualTo(2);
    }

}
