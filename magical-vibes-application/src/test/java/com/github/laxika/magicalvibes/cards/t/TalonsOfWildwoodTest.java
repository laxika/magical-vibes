package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FuneralCharm;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TalonsOfWildwood.class, GreenwoodSentinel.class, Forest.class, FuneralCharm.class})
class TalonsOfWildwoodTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Talons of Wildwood attaches it and grants +1/+1 and trample")
    void resolvingAttachesAndBoosts() {
        Permanent bears = addCreatureReady(player1, new GreenwoodSentinel());

        harness.setHand(player1, List.of(new TalonsOfWildwood()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Talons of Wildwood")
                        && p.isAttached()
                        && bears.getId().equals(p.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses Talons of Wildwood's bonuses when it leaves the battlefield")
    void effectsStopWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new GreenwoodSentinel());
        Permanent talons = new Permanent(new TalonsOfWildwood());
        talons.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(talons);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(talons);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Talons of Wildwood cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new TalonsOfWildwood()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent land = findPermanent(player1, "Forest");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Graveyard ability returns Talons of Wildwood to hand")
    void graveyardAbilityReturnsToHand() {
        harness.setGraveyard(player1, List.of(new TalonsOfWildwood()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Talons of Wildwood");
        harness.assertNotInGraveyard(player1, "Talons of Wildwood");
    }

    @Test
    @DisplayName("Graveyard ability requires {2}{G}")
    void cannotActivateGraveyardAbilityWithoutEnoughMana() {
        harness.setGraveyard(player1, List.of(new TalonsOfWildwood()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canEnchantOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new TalonsOfWildwood()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Talons of Wildwood").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void returnsOnlyTheActivatedCopy() {
        TalonsOfWildwood first = new TalonsOfWildwood();
        TalonsOfWildwood second = new TalonsOfWildwood();
        GreenwoodSentinel creature = new GreenwoodSentinel();
        harness.setGraveyard(player1, List.of(first, second, creature));
        harness.setGraveyard(player2, List.of(new TalonsOfWildwood()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, creature);
        harness.assertInGraveyard(player2, "Talons of Wildwood");
    }

    @Test
    void earlierActivationDoesNotReturnCardDiscardedAfterAnotherActivation() {
        harness.setGraveyard(player1, List.of(new TalonsOfWildwood()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new FuneralCharm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Talons of Wildwood");
        assertThat(gd.stack).hasSize(1);

        harness.castInstant(player2, 0, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Talons of Wildwood");

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Talons of Wildwood");
        harness.assertInGraveyard(player1, "Talons of Wildwood");
    }
}
