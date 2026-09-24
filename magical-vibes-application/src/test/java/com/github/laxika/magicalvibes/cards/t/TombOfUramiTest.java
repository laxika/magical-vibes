package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MikokoroCenterOfTheSea;
import com.github.laxika.magicalvibes.cards.m.MirenTheMoaningWell;
import com.github.laxika.magicalvibes.cards.o.OboroPalaceInTheClouds;
import com.github.laxika.magicalvibes.cards.r.RavingOniSlave;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TombOfUrami.class, RavingOniSlave.class, MikokoroCenterOfTheSea.class,
        MirenTheMoaningWell.class, OboroPalaceInTheClouds.class})
class TombOfUramiTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds black mana and deals damage without an Ogre")
    void manaAbilityDamagesControllerWithoutOgre() {
        harness.addToBattlefield(player1, new TombOfUrami());
        harness.setLife(player1, GameData.STARTING_LIFE_TOTAL);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertLife(player1, GameData.STARTING_LIFE_TOTAL - 1);
    }

    @Test
    @DisplayName("Mana ability does not deal damage while an Ogre is controlled")
    void manaAbilityDoesNotDamageControllerWithOgre() {
        harness.addToBattlefield(player1, new TombOfUrami());
        harness.addToBattlefield(player1, new RavingOniSlave());
        harness.setLife(player1, GameData.STARTING_LIFE_TOTAL);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertLife(player1, GameData.STARTING_LIFE_TOTAL);
    }

    @Test
    @DisplayName("Mana ability still deals damage when only an opponent controls an Ogre")
    void manaAbilityCountsOnlyControllerOgres() {
        harness.addToBattlefield(player1, new TombOfUrami());
        harness.addToBattlefield(player2, new RavingOniSlave());
        harness.setLife(player1, GameData.STARTING_LIFE_TOTAL);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertLife(player1, GameData.STARTING_LIFE_TOTAL - 1);
    }

    @Test
    @DisplayName("Second ability sacrifices all lands, including Tomb of Urami, and creates Urami")
    void secondAbilitySacrificesAllLandsAndCreatesUrami() {
        harness.addToBattlefield(player1, new TombOfUrami());
        harness.addToBattlefield(player1, new MikokoroCenterOfTheSea());
        harness.addToBattlefield(player1, new MirenTheMoaningWell());
        harness.addToBattlefield(player2, new OboroPalaceInTheClouds());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(permanent -> !permanent.getCard().hasType(CardType.LAND));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(3)
                .allMatch(card -> card.hasType(CardType.LAND));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .hasSize(1)
                .allMatch(permanent -> permanent.getCard().hasType(CardType.LAND));

        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(5);
        assertThat(token.getEffectiveToughness()).isEqualTo(5);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DEMON, CardSubtype.SPIRIT);
        assertThat(token.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
    }
}
