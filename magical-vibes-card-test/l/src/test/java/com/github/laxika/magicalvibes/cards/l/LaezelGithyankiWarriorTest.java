package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LaezelGithyankiWarrior.class, Forest.class, GrizzlyBears.class, Island.class,
        Mountain.class, Plains.class, Shock.class, Swamp.class, TreetopVillage.class})
class LaezelGithyankiWarriorTest extends BaseCardTest {

    @Test
    void redFaceCreatesSoldiers() {
        harness.setHand(player1, List.of(new LaezelGithyankiWarrior(), new Mountain()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent laezel = findPermanent(player1, "Lae'zel, Githyanki Warrior");

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

    @Test
    void whiteFaceSeeksOnlyALowManaValueNonlandPermanent() {
        Card eligible = new GrizzlyBears();
        Card expensive = new LaezelGithyankiWarrior();
        Card land = new Forest();
        Card instant = new Shock();
        harness.setLibrary(player1, List.of(eligible, expensive, land, instant));
        harness.setHand(player1, List.of(new LaezelGithyankiWarrior(), new Plains()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(eligible);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(expensive, land, instant);
        harness.assertOnBattlefield(player1, "Lae'zel, Blessed Warrior");
    }

    @Test
    void blueFaceConjuresACopyWithoutRemovingOriginalAndAllowsAnyColorMana() {
        Card original = new GrizzlyBears();
        Card land = new Forest();
        harness.setLibrary(player2, List.of(original, land));
        harness.setHand(player1, List.of(new LaezelGithyankiWarrior(), new Island()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getId()).isNotEqualTo(original.getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(original, land);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void blackSpecializationChoosesGraveyardTargetsBeforeResolvingAndEnforcesTotalManaValue() {
        Card firstBear = new GrizzlyBears();
        Card secondBear = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(firstBear, secondBear, land));
        harness.setHand(player1, List.of(new LaezelGithyankiWarrior(), new Swamp()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(firstBear.getId(), secondBear.getId())
                .doesNotContain(land.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(firstBear.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstBear);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId()).contains(firstBear.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondBear, land)
                .doesNotContain(firstBear);
    }

    @Test
    void mayDeclineTheCastGrantedBlink() {
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
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Lae'zel, Githyanki Warrior")).isSameAs(laezel);
        assertThat(laezel.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void ownSpellDoesNotTriggerTheCastGrantedBlink() {
        harness.setHand(player1, List.of(new LaezelGithyankiWarrior(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent laezel = findPermanent(player1, "Lae'zel, Githyanki Warrior");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, laezel.getId());

        assertThat(findPermanent(player1, "Lae'zel, Githyanki Warrior")).isSameAs(laezel);
        assertThat(laezel.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void enteringWithoutBeingCastDoesNotGrantBlink() {
        harness.addToBattlefield(player1, new LaezelGithyankiWarrior());
        Permanent laezel = findPermanent(player1, "Lae'zel, Githyanki Warrior");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, laezel.getId());

        assertThat(findPermanent(player1, "Lae'zel, Githyanki Warrior")).isSameAs(laezel);
        assertThat(laezel.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void greenFaceBoostsAnAnimatedLandCreature() {
        harness.setHand(player1, List.of(new LaezelGithyankiWarrior(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        Permanent village = findPermanent(player1, "Treetop Village");
        assertThat(gqs.isCreature(gd, village)).isTrue();
        assertThat(gqs.getEffectivePower(gd, village)).isEqualTo(3);

        harness.activateAbility(player1, 0, 4, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, village)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, village)).isEqualTo(4);
    }

    @Test
    void specializedFaceRetainsBlinkAndTriggersAgainOnReturning() {
        harness.setHand(player1, List.of(new LaezelGithyankiWarrior(), new Mountain()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 3, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent laezel = findPermanent(player1, "Lae'zel, Wrathful Warrior");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, laezel.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Lae'zel, Wrathful Warrior");
        assertThat(returned).isNotSameAs(laezel);
        assertThat(returned.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Soldier"))
                .hasSize(4);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, returned.getId());
        assertThat(findPermanent(player1, "Lae'zel, Wrathful Warrior")).isSameAs(returned);
        assertThat(returned.getMarkedDamage()).isEqualTo(2);
    }
}
