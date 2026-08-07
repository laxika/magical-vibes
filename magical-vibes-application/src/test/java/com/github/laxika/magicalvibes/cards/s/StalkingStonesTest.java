package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StalkingStonesTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Stalking Stones produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent stones = addStones(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(stones);

        gs.tapPermanent(gd, player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating the ability makes Stalking Stones a 3/3 artifact creature that's still a land")
    void becomesThreeThreeArtifactCreature() {
        Permanent stones = addStones(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, stones)).isTrue();
        assertThat(gqs.getEffectivePower(gd, stones)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stones)).isEqualTo(3);
        assertThat(gqs.isArtifact(gd, stones)).isTrue();
        assertThat(stones.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    @DisplayName("Animation lasts indefinitely, surviving end-of-turn cleanup")
    void animationSurvivesCleanup() {
        Permanent stones = addStones(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        stones.resetModifiers();

        assertThat(gqs.isCreature(gd, stones)).isTrue();
        assertThat(gqs.getEffectivePower(gd, stones)).isEqualTo(3);
        assertThat(gqs.isArtifact(gd, stones)).isTrue();
    }

    @Test
    @DisplayName("Stalking Stones is not a creature before the ability resolves")
    void notACreatureBeforeActivation() {
        Permanent stones = addStones(player1);

        assertThat(gqs.isCreature(gd, stones)).isFalse();
    }

    private Permanent addStones(Player player) {
        Permanent perm = new Permanent(new StalkingStones());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
