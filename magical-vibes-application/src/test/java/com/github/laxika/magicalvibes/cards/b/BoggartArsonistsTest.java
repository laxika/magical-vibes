package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.OneEyedScarecrow;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoggartArsonistsTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing to destroy a target Scarecrow removes both permanents")
    void destroysTargetScarecrow() {
        Permanent arsonists = addReadyArsonists(player1);
        Permanent scarecrow = addReady(player2, new OneEyedScarecrow());
        addManaForAbility(player1);

        harness.activateAbility(player1, 0, null, scarecrow.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(arsonists);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Boggart Arsonists"));
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(scarecrow);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("One-Eyed Scarecrow"));
    }

    @Test
    @DisplayName("Can destroy a target Plains")
    void destroysTargetPlains() {
        addReadyArsonists(player1);
        Permanent plains = addReady(player2, new Plains());
        addManaForAbility(player1);

        harness.activateAbility(player1, 0, null, plains.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(plains);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Cannot target a non-Scarecrow creature")
    void cannotTargetNonScarecrowCreature() {
        addReadyArsonists(player1);
        Permanent bears = addReady(player2, new GrizzlyBears());
        addManaForAbility(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-Plains land")
    void cannotTargetNonPlainsLand() {
        addReadyArsonists(player1);
        Permanent island = addReady(player2, new Island());
        addManaForAbility(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadyArsonists(player1);
        Permanent plains = addReady(player2, new Plains());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addManaForAbility(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private Permanent addReadyArsonists(Player player) {
        return addReady(player, new BoggartArsonists());
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
