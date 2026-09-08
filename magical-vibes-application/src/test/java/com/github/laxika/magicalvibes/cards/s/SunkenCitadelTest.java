package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed(SunkenCitadel.class)
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

    private Permanent addReadyCitadel(CardColor chosenColor) {
        SunkenCitadel card = new SunkenCitadel();
        Permanent citadel = new Permanent(card);
        citadel.setSummoningSick(false);
        citadel.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player1.getId()).add(citadel);
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
