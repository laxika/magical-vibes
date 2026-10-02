package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mortivore;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.cards.w.WretchedBonemass;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AltarOfTheWretched.class, WretchedBonemass.class, GrizzlyBears.class,
        HillGiant.class, AirElemental.class, WhiteKnight.class, Mortivore.class})
class AltarOfTheWretchedTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may sacrifice a nontoken creature to draw and mill its power")
    void etbSacrificeDrawsAndMillsEqualToPower() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        List<Card> library = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, library);
        castAltar();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Declining the ETB sacrifice does nothing")
    void decliningEtbSacrificeDoesNothing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castAltar();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Craft returns Wretched Bonemass with crafted power and keywords")
    void craftUsesCreaturePowerAndKeywords() {
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new AltarOfTheWretched());
        Permanent flyingMaterial = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent groundMaterial = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1,
                List.of(flyingMaterial.getCard().getId(), groundMaterial.getCard().getId()));
        harness.passBothPriorities();

        Permanent bonemass = findBonemass();
        assertThat(gqs.getEffectivePower(gd, bonemass)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bonemass)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bonemass, Keyword.FLYING)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(altar, flyingMaterial, groundMaterial);
    }

    @Test
    @DisplayName("The graveyard ability returns Altar of the Wretched to hand")
    void returnsFromGraveyardToHand() {
        AltarOfTheWretched altar = new AltarOfTheWretched();
        harness.setGraveyard(player1, List.of(altar));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(altar);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(altar);
    }

    @Test
    @DisplayName("Craft inherits first strike and protection from a graveyard material")
    void craftInheritsProtection() {
        harness.addToBattlefield(player1, new AltarOfTheWretched());
        WhiteKnight material = new WhiteKnight();
        harness.setGraveyard(player1, List.of(material));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(material.getId()));
        harness.passBothPriorities();

        Permanent bonemass = findBonemass();
        assertThat(gqs.getEffectivePower(gd, bonemass)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bonemass, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, bonemass, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, bonemass, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Crafted Bonemass cannot attack immediately without haste")
    void craftedCreatureHasSummoningSickness() {
        harness.addToBattlefield(player1, new AltarOfTheWretched());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(material.getCard().getId()));
        harness.passBothPriorities();

        assertThat(als.canAttack(gd, findBonemass(), player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Bonemass uses and updates an exiled material's characteristic-defined power")
    void craftUsesCharacteristicDefinedPower() {
        harness.addToBattlefield(player1, new AltarOfTheWretched());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Mortivore mortivore = new Mortivore();
        harness.setGraveyard(player1, List.of(mortivore, new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(material.getCard().getId(), mortivore.getId()));
        harness.passBothPriorities();

        Permanent bonemass = findBonemass();
        assertThat(gqs.getEffectivePower(gd, bonemass)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bonemass)).isEqualTo(6);
        harness.setGraveyard(player2, List.of(new HillGiant(), new GrizzlyBears()));
        assertThat(gqs.getEffectivePower(gd, bonemass)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bonemass)).isEqualTo(7);
    }

    @Test
    @DisplayName("ETB uses the sacrificed creature's power including counters")
    void etbUsesLastKnownPower() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castAltar();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerLibraries.get(player1.getId())).isEmpty();
    }

    private Permanent findBonemass() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isTransformed() && permanent.getCard() instanceof WretchedBonemass)
                .findFirst()
                .orElseThrow();
    }

    private void castAltar() {
        harness.castFromHand(player1, new AltarOfTheWretched(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
