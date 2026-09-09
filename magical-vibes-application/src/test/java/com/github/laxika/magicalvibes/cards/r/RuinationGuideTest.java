package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MistIntruder;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuinationGuide.class, MistIntruder.class, GrizzlyBears.class})
class RuinationGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Other colorless creatures you control get +1/+0")
    void buffsOtherColorlessCreaturesYouControl() {
        harness.addToBattlefield(player1, new RuinationGuide());
        harness.addToBattlefield(player1, new MistIntruder());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new MistIntruder());

        Permanent guide = findPermanent(player1, "Ruination Guide");
        Permanent colorlessCreature = findPermanent(player1, "Mist Intruder");
        Permanent coloredCreature = findPermanent(player1, "Grizzly Bears");
        Permanent opponentColorlessCreature = findPermanent(player2, "Mist Intruder");

        assertThat(gqs.getEffectivePower(gd, guide)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guide)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, colorlessCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, colorlessCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, coloredCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentColorlessCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage exiles the top card of the damaged player's library")
    void combatDamageExilesTopCard() {
        Permanent guide = addAttackingGuide(player1);
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.sourcePermanentId()).isNull();
        assertThat(gd.getCardsExiledByPermanent(guide.getId())).isEmpty();
    }

    private Permanent addAttackingGuide(Player player) {
        Permanent guide = addCreatureReady(player, new RuinationGuide());
        guide.setAttacking(true);
        return guide;
    }
}
