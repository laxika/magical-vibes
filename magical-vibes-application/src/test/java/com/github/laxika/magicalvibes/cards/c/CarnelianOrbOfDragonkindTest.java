package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CarnelianOrbOfDragonkind.class)
class CarnelianOrbOfDragonkindTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Carnelian Orb of Dragonkind adds red mana")
    void tappingAddsRedMana() {
        Permanent orb = addReadyOrb();

        harness.activateAbility(player1, 0, null, null);

        assertThat(manaPool().get(ManaColor.RED)).isEqualTo(1);
        assertThat(orb.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana spent on a Dragon creature spell grants it haste")
    void dragonCastWithOrbManaGainsHaste() {
        addReadyOrb();
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(createCreature("Test Dragon", CardSubtype.DRAGON)));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Test Dragon").hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Mana spent on a non-Dragon creature spell does not grant haste")
    void nonDragonCastWithOrbManaDoesNotGainHaste() {
        addReadyOrb();
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(createCreature("Test Goblin", CardSubtype.GOBLIN)));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Test Goblin").hasKeyword(Keyword.HASTE)).isFalse();
    }

    private Permanent addReadyOrb() {
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new CarnelianOrbOfDragonkind());
        orb.setSummoningSick(false);
        return orb;
    }

    private ManaPool manaPool() {
        return gd.playerManaPools.get(player1.getId());
    }

    private static Card createCreature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
