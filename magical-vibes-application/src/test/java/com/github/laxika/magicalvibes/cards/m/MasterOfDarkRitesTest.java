package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MasterOfDarkRites.class)
class MasterOfDarkRitesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature adds three black subtype-restricted mana")
    void sacrificesAnotherCreatureForRestrictedMana() {
        Permanent master = addMasterAndFodder();

        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(master.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(pool.get(ManaColor.BLACK)).isZero();
        assertThat(pool.getSubtypeSpellOnlyManaForColor(
                Set.of(CardSubtype.VAMPIRE, CardSubtype.CLERIC, CardSubtype.DEMON), ManaColor.BLACK))
                .isEqualTo(3);
    }

    @Test
    @DisplayName("Restricted mana casts Vampire, Cleric, and Demon spells")
    void restrictedManaCastsAllowedSubtypes() {
        addMasterAndFodder();
        harness.activateAbility(player1, 0, 0, null, null);

        for (CardSubtype subtype : List.of(CardSubtype.VAMPIRE, CardSubtype.CLERIC, CardSubtype.DEMON)) {
            harness.setHand(player1, List.of(creature("Test " + subtype, subtype)));
            harness.castCreature(player1, 0);
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Restricted mana cannot cast an unrelated creature spell")
    void restrictedManaRejectsOtherSubtypes() {
        addMasterAndFodder();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(creature("Test Bear", CardSubtype.BEAR)));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot sacrifice the source itself")
    void requiresAnotherCreature() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new MasterOfDarkRites());
        master.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addMasterAndFodder() {
        Permanent master = harness.addToBattlefieldAndReturn(player1, new MasterOfDarkRites());
        master.setSummoningSick(false);
        harness.addToBattlefield(player1, creature("Sacrifice Fodder", CardSubtype.BEAR));
        return master;
    }

    private static Card creature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{B}");
        card.setColor(CardColor.BLACK);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
