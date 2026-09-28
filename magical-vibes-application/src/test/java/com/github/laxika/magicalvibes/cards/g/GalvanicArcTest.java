package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GalvanicArc.class, GiantSpider.class, HowlingMine.class, RuneclawBear.class})
class GalvanicArcTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has first strike and the ETB trigger deals 3 damage to a player")
    void grantsFirstStrikeAndDamagesPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setLife(player2, 20);

        castGalvanicArc(creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The ETB trigger deals 3 damage to a creature")
    void damagesCreature() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        castGalvanicArc(enchantedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        assertThat(gqs.hasKeyword(gd, enchantedCreature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @CardUsed(ChandraNalaar.class)
    @DisplayName("The ETB trigger deals 3 damage to a planeswalker")
    void damagesPlaneswalker() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);

        castGalvanicArc(enchantedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gqs.hasKeyword(gd, enchantedCreature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The ETB trigger may target the enchanted creature")
    void mayTargetEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

        castGalvanicArc(creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The ETB damage target must be a creature, planeswalker, or player")
    void rejectsInvalidEtbTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        castGalvanicArc(creature.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    private void castGalvanicArc(UUID enchantTargetId) {
        harness.setHand(player1, List.of(new GalvanicArc()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, enchantTargetId);
    }
}
