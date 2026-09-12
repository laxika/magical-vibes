package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeothermalKami.class, GloriousAnthem.class})
class GeothermalKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Returning an enchantment gains 3 life")
    void returningEnchantmentGainsLife() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.setLife(player1, 10);
        castAndResolve();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(anthem.getId());
        harness.handlePermanentChosen(player1, anthem.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Glorious Anthem");
        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Declining the return gains no life")
    void decliningReturnGainsNoLife() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.setLife(player1, 10);
        castAndResolve();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("No enchantment means no life gain")
    void noEnchantmentMeansNoLifeGain() {
        harness.setLife(player1, 10);
        castAndResolve();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Geothermal Kami");
        harness.assertLife(player1, 10);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new GeothermalKami()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
