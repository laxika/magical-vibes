package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WestvaleAbbeyTest extends BaseCardTest {

    private static final int TOKEN_ABILITY = 1;
    private static final int TRANSFORM_ABILITY = 2;

    @Test
    @DisplayName("Token ability pays 1 life and creates a 1/1 Human Cleric")
    void tokenAbilityCreatesHumanCleric() {
        Permanent abbey = addAbbey(player1);
        harness.setLife(player1, GameData.STARTING_LIFE_TOTAL);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, indexOf(abbey), TOKEN_ABILITY, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(GameData.STARTING_LIFE_TOTAL - 1);
        Permanent token = findPermanent(player1, "Human Cleric");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Transform ability sacrifices five creatures, transforms and untaps the land")
    void transformSacrificesFiveCreatures() {
        Permanent abbey = addAbbey(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new LlanowarElves());
        }

        harness.activateAbility(player1, indexOf(abbey), TRANSFORM_ABILITY, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Llanowar Elves")).hasSize(5);
        assertThat(abbey.isTransformed()).isTrue();
        assertThat(abbey.getCard().getName()).isEqualTo("Ormendahl, Profane Prince");
        assertThat(abbey.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Transform ability cannot be activated with only four creatures")
    void transformNeedsFiveCreatures() {
        Permanent abbey = addAbbey(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new LlanowarElves());
        }

        int index = indexOf(abbey);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, TRANSFORM_ABILITY, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(abbey.isTransformed()).isFalse();
    }

    private Permanent addAbbey(Player player) {
        Permanent perm = new Permanent(new WestvaleAbbey());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }
}
