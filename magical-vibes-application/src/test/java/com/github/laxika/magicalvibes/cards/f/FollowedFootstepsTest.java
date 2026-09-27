package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SelesnyaSignet;
import com.github.laxika.magicalvibes.cards.s.SiegeWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FollowedFootsteps.class, SiegeWurm.class, SelesnyaSignet.class})
class FollowedFootstepsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of the enchanted creature at your upkeep")
    void createsTokenCopyOfEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());
        castFollowedFootsteps(creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Siege Wurm"))
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getEffectivePower()).isEqualTo(5);
                    assertThat(token.getEffectiveToughness()).isEqualTo(5);
                });
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SiegeWurm());
        castFollowedFootsteps(creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Siege Wurm")).isEmpty();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new SelesnyaSignet());
        harness.setHand(player1, List.of(new FollowedFootsteps()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castFollowedFootsteps(Permanent creature) {
        harness.setHand(player1, List.of(new FollowedFootsteps()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
