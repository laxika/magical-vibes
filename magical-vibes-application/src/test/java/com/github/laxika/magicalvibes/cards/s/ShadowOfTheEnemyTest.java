package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadowOfTheEnemy.class, Forest.class, GrizzlyBears.class})
class ShadowOfTheEnemyTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles only creature cards from the target player's graveyard")
    void exilesOnlyCreatureCards() {
        GrizzlyBears creature = new GrizzlyBears();
        Forest land = new Forest();
        harness.setGraveyard(player2, List.of(creature, land));
        castShadow(player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(land);
        assertThat(gd.exilePlayPermissions).containsEntry(creature.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(creature.getId());
    }

    @Test
    @DisplayName("Casts an exiled creature with mana of any type after the spell resolves")
    void castsExiledCreatureWithAnyManaAfterSpellResolves() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        castShadow(player2.getId());

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(creature.getId())).isNull();
    }

    private void castShadow(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new ShadowOfTheEnemy()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }
}
