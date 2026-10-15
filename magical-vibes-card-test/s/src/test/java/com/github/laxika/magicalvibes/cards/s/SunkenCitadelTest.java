package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CaptivatingCave;
import com.github.laxika.magicalvibes.cards.c.CompassGnome;
import com.github.laxika.magicalvibes.cards.d.DampingSphere;
import com.github.laxika.magicalvibes.cards.h.HuntersBlowgun;
import com.github.laxika.magicalvibes.cards.r.RestlessPrairie;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunkenCitadel.class, CaptivatingCave.class, CompassGnome.class,
        HuntersBlowgun.class, RestlessPrairie.class, Spelunking.class, DampingSphere.class})
class SunkenCitadelTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and stores the chosen color")
    void entersTappedAndStoresChosenColor() {
        harness.setHand(player1, List.of(new SunkenCitadel()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        Permanent citadel = findPermanent(player1, "Sunken Citadel");
        assertThat(citadel.isTapped()).isTrue();
        assertThat(citadel.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("The first ability adds one mana of the chosen color")
    void firstAbilityAddsChosenColorMana() {
        addReadyCitadel(CardColor.GREEN);

        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.getLandAbilityOnlyManaTotal()).isZero();
    }

    @Test
    @DisplayName("The second ability adds two mana restricted to land abilities")
    void secondAbilityAddsLandAbilityOnlyMana() {
        addReadyCitadel(CardColor.GREEN);

        harness.activateAbility(player1, 0, 1, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.getLandAbilityOnlyMana(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Restricted mana pays for land abilities but not nonland abilities")
    void restrictedManaOnlyPaysForLandAbilities() {
        addReadyCitadel(CardColor.GREEN);
        harness.activateAbility(player1, 0, 1, null, null);

        Permanent landSource = harness.addToBattlefieldAndReturn(player1, abilitySource(true));
        landSource.setSummoningSick(false);
        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getLandAbilityOnlyManaTotal()).isEqualTo(1);

        Permanent nonlandSource = harness.addToBattlefieldAndReturn(player1, abilitySource(false));
        nonlandSource.setSummoningSick(false);
        assertThatThrownBy(() -> harness.activateAbility(player1, 2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana cannot cast a spell even when its generic cost matches")
    void restrictedManaCannotCastSpell() {
        addReadyCitadel(CardColor.BLUE);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.setHand(player1, List.of(new CompassGnome()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getLandAbilityOnlyMana(ManaColor.BLUE))
                .isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Restricted mana cannot pay an Equipment's equip cost")
    void restrictedManaCannotEquip() {
        addReadyCitadel(CardColor.RED);
        harness.addToBattlefield(player1, new HuntersBlowgun());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CompassGnome());
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getLandAbilityOnlyMana(ManaColor.RED))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Restricted mana pays a land mana ability and unused mana stays restricted")
    void restrictedManaPaysLandManaAbility() {
        addReadyCitadel(CardColor.BLACK);
        harness.addToBattlefield(player1, new CaptivatingCave());
        harness.activateAbility(player1, 0, 1, null, null);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "WHITE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.BLACK)).isZero();
        assertThat(pool.getLandAbilityOnlyMana(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Restricted mana can pay colored and generic costs of an animated land")
    void restrictedManaPaysColoredCostsOfAnimatedLand() {
        Permanent citadel = addReadyCitadel(CardColor.GREEN);
        Permanent prairie = harness.addToBattlefieldAndReturn(player1, new RestlessPrairie());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, prairie)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getLandAbilityOnlyManaTotal()).isZero();

        citadel.untap();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, prairie)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getLandAbilityOnlyManaTotal()).isZero();
    }

    @Test
    @DisplayName("Spelunking permits immediate mana production after the entry color choice")
    void entersUntappedWithSpelunkingAndProducesChosenMana() {
        harness.addToBattlefield(player1, new Spelunking());
        harness.setHand(player1, List.of(new SunkenCitadel()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, "RED");

        Permanent citadel = findPermanent(player1, "Sunken Citadel");
        assertThat(citadel.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.activateAbility(player1, 1, 0, null, null);
        assertThat(citadel.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damping Sphere changes mana type and amount but preserves the spending restriction")
    void dampingSpherePreservesLandAbilityRestriction() {
        addReadyCitadel(CardColor.BLUE);
        harness.addToBattlefield(player1, new DampingSphere());

        harness.activateAbility(player1, 0, 1, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
        assertThat(pool.getLandAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(pool.getLandAbilityOnlyMana(ManaColor.BLUE)).isZero();

        harness.addToBattlefield(player1, new CaptivatingCave());
        harness.activateAbility(player1, 2, 1, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(pool.getLandAbilityOnlyManaTotal()).isZero();
        assertThat(pool.get(ManaColor.WHITE)).isEqualTo(1);
    }

    private Permanent addReadyCitadel(CardColor chosenColor) {
        Permanent citadel = harness.addToBattlefieldAndReturn(player1, new SunkenCitadel());
        citadel.setSummoningSick(false);
        citadel.setChosenColor(chosenColor);
        return citadel;
    }

    private static Card abilitySource(boolean land) {
        Card card = new Card();
        card.setName(land ? "Test Land" : "Test Artifact");
        card.setType(land ? CardType.LAND : CardType.ARTIFACT);
        card.setManaCost("{0}");
        card.addActivatedAbility(new ActivatedAbility(
                false, "{1}", List.of(new GainLifeEffect(1)), "{1}: You gain 1 life."));
        return card;
    }
}
