package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MerfolkAssassin.class, GrizzlyBears.class})
class MerfolkAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature with islandwalk")
    void destroysTargetCreatureWithIslandwalk() {
        addCreatureReady(player1, new MerfolkAssassin());
        Permanent target = addIslandwalkCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Islandwalk Creature");
    }

    @Test
    @DisplayName("Cannot target a creature without islandwalk")
    void cannotTargetCreatureWithoutIslandwalk() {
        addCreatureReady(player1, new MerfolkAssassin());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with islandwalk");
    }

    private Permanent addIslandwalkCreature(Player player) {
        Card card = new Card();
        card.setName("Islandwalk Creature");
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(Set.of(Keyword.ISLANDWALK));
        return addCreatureReady(player, card);
    }
}
