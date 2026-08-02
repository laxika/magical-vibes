package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClearAPathTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with defender")
    void destroysCreatureWithDefender() {
        Permanent wall = addCreature(player2, "Test Wall", true);

        prepare();
        harness.castSorcery(player1, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(wall.getId()));
        harness.assertInGraveyard(player2, "Test Wall");
    }

    @Test
    @DisplayName("Cannot target a creature without defender")
    void cannotTargetCreatureWithoutDefender() {
        Permanent bear = addCreature(player2, "Test Bear", false);

        prepare();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A regeneration shield saves the creature")
    void regenerationShieldSavesTheCreature() {
        Permanent wall = addCreature(player2, "Test Wall", true);
        wall.setRegenerationShield(1);

        prepare();
        harness.castSorcery(player1, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(wall.getId()));
    }

    private void prepare() {
        harness.setHand(player1, List.of(new ClearAPath()));
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private Permanent addCreature(Player player, String name, boolean defender) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(0);
        card.setToughness(4);
        if (defender) {
            card.setKeywords(java.util.Set.of(Keyword.DEFENDER));
        }
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
