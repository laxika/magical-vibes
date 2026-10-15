package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Glimmerpost;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlpineMoon.class, Forest.class, Glimmerpost.class, GrizzlyBears.class, Island.class})
class AlpineMoonTest extends BaseCardTest {

    @Test
    @DisplayName("As it enters, Alpine Moon offers only nonbasic land names")
    void choosesNonbasicLandName() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AlpineMoon()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("Glimmerpost").doesNotContain("Forest", "Grizzly Bears");

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Forest"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid nonbasic land card name");

        harness.handleListChoice(player1, "Glimmerpost");

        assertThat(findPermanent(player1, "Alpine Moon").getChosenName()).isEqualTo("Glimmerpost");
    }

    @Test
    @DisplayName("Matching opponent lands lose their types and abilities but gain any-color mana")
    void changesMatchingOpponentLands() {
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Glimmerpost());
        opponentLand.setSummoningSick(false);
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Glimmerpost());
        ownLand.setSummoningSick(false);

        Permanent alpineMoon = harness.addToBattlefieldAndReturn(player1, new AlpineMoon());
        alpineMoon.setChosenName("Glimmerpost");

        var opponentBonus = gqs.computeStaticBonus(gd, opponentLand);
        assertThat(opponentBonus.losesAllAbilities()).isTrue();
        assertThat(opponentBonus.landSubtypeOverriding()).isTrue();
        assertThat(opponentBonus.grantedSubtypes()).isEmpty();
        assertThat(gqs.effectiveBasicLandTypes(gd, opponentLand)).isEmpty();
        assertThat(opponentBonus.grantedActivatedAbilities()).isNotEmpty();
        assertThat(gqs.isLand(gd, opponentLand)).isTrue();

        assertThat(gqs.computeStaticBonus(gd, ownLand).losesAllAbilities()).isFalse();

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "BLUE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(opponentLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An existing Alpine Moon does not prevent a basic land from entering")
    void basicLandCanEnterWhileAlpineMoonIsOnBattlefield() {
        Permanent alpineMoon = harness.addToBattlefieldAndReturn(player2, new AlpineMoon());
        alpineMoon.setChosenName("Glimmerpost");

        Island island = new Island();
        harness.setHand(player1, List.of(island));
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getOriginalCard))
                .contains(island);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(island);
    }

    @Test
    void matchingLandEnteringDoesNotTriggerItsLifeGain() {
        harness.setHand(player1, List.of(new AlpineMoon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Glimmerpost");

        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Glimmerpost()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
        Permanent land = findPermanent(player2, "Glimmerpost");
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.LOCUS)).isFalse();
        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "GREEN");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void unmatchedBasicLandKeepsItsManaAbility() {
        Permanent moon = harness.addToBattlefieldAndReturn(player1, new AlpineMoon());
        moon.setChosenName("Glimmerpost");
        harness.addToBattlefield(player2, new Island());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, findPermanent(player2, "Island"), CardSubtype.ISLAND)).isTrue();
    }

    @Test
    void landRegainsItsTypesAndPrintedManaAbilityWhenMoonLeaves() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Glimmerpost());
        Permanent moon = harness.addToBattlefieldAndReturn(player1, new AlpineMoon());
        moon.setChosenName("Glimmerpost");
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.LOCUS)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(moon);
        gd.playerGraveyards.get(player1.getId()).add(moon.getCard());

        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.LOCUS)).isTrue();
        harness.tapPermanent(player2, 0);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, land).grantedActivatedAbilities()).isEmpty();
    }
}
