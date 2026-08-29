package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OggyarBattleSeerTest extends BaseCardTest {

    @Test
    void activatingAbilityTapsBattleSeerAndPutsAbilityOnStack() {
        Permanent seer = addCreatureReady(player1, new OggyarBattleSeer());

        harness.activateAbility(player1, 0, null, null);

        assertThat(seer.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void resolvingAbilityStartsScryOneInteraction() {
        addCreatureReady(player1, new OggyarBattleSeer());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(1);
    }
}
