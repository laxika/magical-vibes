package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YouFindACursedIdol.class, FountainOfYouth.class, GloriousAnthem.class})
class YouFindACursedIdolTest extends BaseCardTest {

    @Test
    void smashItDestroysAnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        cast(0, artifact.getId());

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    void liftTheCurseDestroysAnEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(1, enchantment.getId());

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    void stealItsEyesCreatesTreasureAndVenturesIntoTheDungeon() {
        cast(2);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    void destructionModesOnlyAllowTheirSpecifiedPermanentType() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);

        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int modeIndex, java.util.UUID targetId) {
        prepareSpell();
        harness.castModalInstant(player1, 0, modeIndex, List.of(targetId));
        harness.passBothPriorities();
    }

    private void cast(int modeIndex) {
        prepareSpell();
        harness.castModalInstant(player1, 0, modeIndex, List.of());
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new YouFindACursedIdol()));
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
