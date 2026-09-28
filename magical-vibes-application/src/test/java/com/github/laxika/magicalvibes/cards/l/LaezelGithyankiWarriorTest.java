package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LaezelGithyankiWarrior.class, Forest.class, GrizzlyBears.class, Mountain.class, Shock.class})
class LaezelGithyankiWarriorTest extends BaseCardTest {

    @Test
    void hasFiveSpecializeAbilitiesAndRedFaceCreatesSoldiers() {
        harness.setHand(player1, List.of(new LaezelGithyankiWarrior(), new Mountain()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent laezel = findPermanent(player1, "Lae'zel, Githyanki Warrior");
        assertThat(laezel.getCard().getActivatedAbilities()).hasSize(5);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(laezel.getCard().getName()).isEqualTo("Lae'zel, Wrathful Warrior");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Soldier"))
                .hasSize(2);
    }

    @Test
    void greenFacePerpetuallyBoostsOtherCreaturesAndFutureCreatureCardsFromHand() {
        GrizzlyBears otherCreature = new GrizzlyBears();
        GrizzlyBears creatureInHand = new GrizzlyBears();
        harness.setHand(player1, List.of(new LaezelGithyankiWarrior(), new Forest(), creatureInHand));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent laezel = findPermanent(player1, "Lae'zel, Githyanki Warrior");
        harness.addToBattlefield(player1, otherCreature);

        harness.activateAbility(player1, 0, 4, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(laezel.getCard().getName()).isEqualTo("Lae'zel, Primal Warrior");
        Permanent other = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .allSatisfy(permanent -> {
                    assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(3);
                    assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(3);
                });
    }

    @Test
    void castGrantedAbilityFlickersLaezelWhenTargetedByAnOpponent() {
        harness.setHand(player1, List.of(new LaezelGithyankiWarrior()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent laezel = findPermanent(player1, "Lae'zel, Githyanki Warrior");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, laezel.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Lae'zel, Githyanki Warrior")).isNotSameAs(laezel);
        assertThat(findPermanent(player1, "Lae'zel, Githyanki Warrior").getMarkedDamage()).isZero();
    }
}
