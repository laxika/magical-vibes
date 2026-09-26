package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChainLightning;
import com.github.laxika.magicalvibes.cards.c.Cleanse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Backdraft.class, ChainLightning.class, Cleanse.class})
class BackdraftTest extends BaseCardTest {

    @Test
    void dealsHalfTheDamageDealtByOneSorceryRoundedDown() {
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.setHand(player2, List.of(new Backdraft()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    void choosesAnEligiblePlayerWhenItResolves() {
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.setHand(player2, List.of(new Backdraft()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.passPriority(player1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPlayerIds()).containsExactly(player1.getId());

        harness.handlePermanentChosen(player2, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    void aSorceryNeedNotHaveDealtDamageToQualify() {
        harness.setHand(player1, List.of(new Cleanse()));
        harness.setHand(player2, List.of(new Backdraft()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}
