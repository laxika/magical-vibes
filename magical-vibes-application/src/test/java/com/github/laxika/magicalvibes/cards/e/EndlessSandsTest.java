package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EndlessSandsTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C}")
    void tapAddsColorless() {
        harness.addToBattlefieldAndReturn(player1, new EndlessSands());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("{2}, {T}: Exile target creature you control, tracked with Endless Sands")
    void exileAbilityExilesOwnCreature() {
        Permanent sands = harness.addToBattlefieldAndReturn(player1, new EndlessSands());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getCardsExiledByPermanent(sands.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("{2}, {T} cannot target a creature you don't control")
    void exileAbilityCannotTargetOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new EndlessSands());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent enemyBears = findPermanent(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, enemyBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{4}, {T}, Sacrifice: return creatures exiled with this land under owners' control")
    void sacrificeReturnsExiledCreatures() {
        Permanent sands = harness.addToBattlefieldAndReturn(player1, new EndlessSands());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.getCardsExiledByPermanent(sands.getId())).hasSize(1);

        sands.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Endless Sands"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Endless Sands"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getCardsExiledByPermanent(sands.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice with no exiled cards still sacrifices the land")
    void sacrificeWithNoExiledCards() {
        harness.addToBattlefieldAndReturn(player1, new EndlessSands());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Endless Sands"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Endless Sands"));
    }
}
