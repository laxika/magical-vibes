package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HerdHeirloomTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one creature-spell-only mana of the chosen color")
    void addsCreatureSpellOnlyMana() {
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new HerdHeirloom());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(heirloom.isTapped()).isTrue();
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Grants trample and a temporary combat-damage draw trigger to a qualifying creature")
    void grantsTemporaryCombatDamageDraw() {
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new HerdHeirloom());
        Permanent lowPowerCreature = addCreatureReady(player1, creature("Small Beast", 3));
        Permanent qualifyingCreature = addCreatureReady(player1, creature("Large Beast", 4));
        Permanent opposingCreature = addCreatureReady(player2, creature("Opposing Beast", 4));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, lowPowerCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, 1, null, qualifyingCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, qualifyingCreature, Keyword.TRAMPLE)).isTrue();

        declareAttackers(List.of(2));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        qualifyingCreature.resetModifiers();
        assertThat(gqs.hasKeyword(gd, qualifyingCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(heirloom.isTapped()).isTrue();
    }

    private static Card creature(String name, int power) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{" + power + "}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(power);
        return card;
    }
}
