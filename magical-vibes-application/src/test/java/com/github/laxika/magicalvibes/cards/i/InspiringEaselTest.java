package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InspiringEasel.class, GrizzlyBears.class, LightningBolt.class})
class InspiringEaselTest extends BaseCardTest {

    @Test
    void producesAnyColorManaRestrictedToInstantAndSorcerySpells() {
        Permanent easel = harness.addToBattlefieldAndReturn(player1, new InspiringEasel());
        easel.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.BLUE))
                .isEqualTo(1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    void selectedSpellPerpetuallyIncorporatesCostAndCopiesWhenCast() {
        Permanent easel = harness.addToBattlefieldAndReturn(player1, new InspiringEasel());
        easel.setSummoningSick(false);
        LightningBolt chosen = new LightningBolt();
        harness.setHand(player1, List.of(new GrizzlyBears(), chosen));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.PerpetualPowerToughnessChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PerpetualPowerToughnessChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        ManaCost incorporated = gd.perpetualManaCostIncreases.get(chosen.getId());
        assertThat(incorporated.countColorSymbols(ManaColor.BLUE)).isEqualTo(1);
        assertThat(incorporated.countColorSymbols(ManaColor.RED)).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 1, player2.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getDescription() != null
                && entry.getDescription().startsWith("Copy Lightning Bolt"));
    }
}
