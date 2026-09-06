package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Slash, Reptile Rampager")
@CardUsed({SlashReptileRampager.class, GrizzlyBears.class})
class SlashReptileRampagerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger when Slash itself enters the battlefield")
    void doesNotTriggerOnItsOwnEntry() {
        harness.setHand(player1, List.of(new SlashReptileRampager()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals 2 damage to each opponent when another creature enters under its controller's control")
    void dealsDamageWhenAnotherCreatureEnters() {
        harness.addToBattlefield(player1, new SlashReptileRampager());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Creates a 2/2 red Mutant token when it attacks")
    void attackingCreatesMutantToken() {
        addCreatureReady(player1, new SlashReptileRampager());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Mutant").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(2);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(2);
        assertThat(tokens.getFirst().getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(tokens.getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.MUTANT);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }
}
