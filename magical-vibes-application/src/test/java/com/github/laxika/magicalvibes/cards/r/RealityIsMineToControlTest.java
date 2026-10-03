package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RealityIsMineToControl.class, DarkRitual.class, GrizzlyBears.class})
class RealityIsMineToControlTest extends BaseCardTest {

    @Test
    void acceptingAbandonsSchemeAndCopiesSpell() {
        Permanent scheme = addScheme();
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, scheme.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scheme);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(scheme.getCard());
        assertThat(gd.stack).filteredOn(entry -> entry.getCard() instanceof DarkRitual).hasSize(2);
    }

    @Test
    void decliningKeepsSchemeAndDoesNotCopySpell() {
        Permanent scheme = addScheme();
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scheme);
        assertThat(gd.stack).filteredOn(entry -> entry.getCard() instanceof DarkRitual).hasSize(1);
    }

    @Test
    void copyingPermanentSpellCreatesTokenPermanent() {
        Permanent scheme = addScheme();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, scheme.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .hasSize(2)
                .anyMatch(permanent -> permanent.getCard().isToken());
    }

    private Permanent addScheme() {
        return harness.addToBattlefieldAndReturn(player1, new RealityIsMineToControl());
    }
}
