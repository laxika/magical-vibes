package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForswornPaladin.class, WilyGoblin.class})
class ForswornPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability costs 1 life and creates a Treasure")
    void createsTreasureAndPaysLife() {
        Permanent paladin = addCreatureReady(player1, new ForswornPaladin());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(paladin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability pumps a target creature without granting deathtouch from ordinary mana")
    void ordinaryManaDoesNotGrantDeathtouch() {
        Permanent paladin = addCreatureReady(player1, new ForswornPaladin());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, paladin.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("The second ability grants deathtouch when Treasure mana pays its colored cost")
    void treasureManaGrantsDeathtouch() {
        Permanent paladin = addCreatureReady(player1, new ForswornPaladin());
        harness.setHand(player1, List.of(new WilyGoblin()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player1, "Treasure");
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "BLACK");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, paladin.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paladin)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.DEATHTOUCH)).isTrue();
    }
}
