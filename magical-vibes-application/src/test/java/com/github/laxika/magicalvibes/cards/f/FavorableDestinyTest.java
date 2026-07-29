package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FavorableDestinyTest extends BaseCardTest {

    @Test
    @DisplayName("White enchanted creature gets +1/+2")
    void whiteCreatureGetsBoost() {
        Permanent vanguard = addCreatureReady(player1, new EliteVanguard()); // 2/1 white
        attach(player1, vanguard);

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nonwhite enchanted creature gets no boost")
    void nonWhiteCreatureGetsNoBoost() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2 green
        attach(player1, bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("No shroud when the enchanted creature is the only creature its controller has")
    void noShroudWithoutAnotherCreature() {
        Permanent vanguard = addCreatureReady(player1, new EliteVanguard());
        attach(player1, vanguard);

        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud while the enchanted creature's controller controls another creature")
    void shroudWithAnotherCreature() {
        Permanent vanguard = addCreatureReady(player1, new EliteVanguard());
        attach(player1, vanguard);
        addCreatureReady(player1, new SuntailHawk());

        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Another creature controlled by a different player does not grant shroud")
    void otherPlayersCreatureDoesNotGrantShroud() {
        Permanent vanguard = addCreatureReady(player1, new EliteVanguard());
        attach(player1, vanguard);
        addCreatureReady(player2, new SuntailHawk());

        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud follows the enchanted creature's controller, not the Aura's controller")
    void shroudReadsEnchantedCreaturesController() {
        Permanent vanguard = addCreatureReady(player2, new EliteVanguard());
        attach(player1, vanguard);
        addCreatureReady(player1, new SuntailHawk());

        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.SHROUD)).isFalse();

        addCreatureReady(player2, new SuntailHawk());

        assertThat(gqs.hasKeyword(gd, vanguard, Keyword.SHROUD)).isTrue();
    }

    private void attach(com.github.laxika.magicalvibes.model.Player controller, Permanent creature) {
        Permanent aura = new Permanent(new FavorableDestiny());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }
}
