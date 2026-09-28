package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KoboldWarcaller.class, GrizzlyBears.class, Shock.class})
class KoboldWarcallerTest extends BaseCardTest {

    @Test
    void perpetuallyGrantsHasteToAChosenCreatureCardInHand() {
        GrizzlyBears bears = new GrizzlyBears();
        addCreatureReady(player1, new KoboldWarcaller());
        harness.setHand(player1, List.of(new Shock(), bears));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PerpetualPowerToughnessChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PerpetualPowerToughnessChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 1);
        harness.passBothPriorities();

        Permanent enteredBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bears.getId()))
                .findFirst().orElseThrow();
        assertThat(enteredBears.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotPromptWhenHandHasNoCreatureCard() {
        addCreatureReady(player1, new KoboldWarcaller());
        harness.setHand(player1, List.of(new Shock()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
