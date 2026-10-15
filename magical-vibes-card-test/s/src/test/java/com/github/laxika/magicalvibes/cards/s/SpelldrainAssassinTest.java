package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpelldrainAssassin.class, FyndhornElves.class, GrizzlyBears.class, LightningBolt.class})
class SpelldrainAssassinTest extends BaseCardTest {

    @Test
    void choosesAnInstantOrSorceryAndGrantsItCasualtyTwo() {
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpelldrainAssassin(), new LightningBolt(), new GrizzlyBears()));
        addAssassinMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PerpetualPowerToughnessChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PerpetualPowerToughnessChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castWithCasualty(player1, 0, player2.getId(), List.of(fodder.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(fodder.getId()));
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getEffectsToResolve().stream().anyMatch(CopyControllerCastSpellEffect.class::isInstance));
    }

    @Test
    void casualtyTwoRequiresAControllableCreatureWithPowerTwo() {
        Permanent fodder = addCreatureReady(player1, new FyndhornElves());
        harness.setHand(player1, List.of(new SpelldrainAssassin(), new LightningBolt()));
        addAssassinMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithCasualty(
                player1, 0, player2.getId(), List.of(fodder.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
    }

    private void addAssassinMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
