package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnheloThePainter.class, FyndhornElves.class, GrizzlyBears.class, LightningBolt.class})
class AnheloThePainterTest extends BaseCardTest {

    @Test
    @DisplayName("The first instant or sorcery each turn has casualty 2")
    void firstInstantOrSorceryHasCasualtyTwo() {
        harness.addToBattlefield(player1, new AnheloThePainter());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithCasualty(player1, 0, player2.getId(), List.of(fodder.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(fodder.getId()));
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getEffectsToResolve().stream().anyMatch(CopyControllerCastSpellEffect.class::isInstance));
    }

    @Test
    @DisplayName("Anhelo's casualty cannot be paid with a creature below power 2")
    void casualtyRequiresPowerTwo() {
        harness.addToBattlefield(player1, new AnheloThePainter());
        Permanent fodder = addCreatureReady(player1, new FyndhornElves());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithCasualty(player1, 0, player2.getId(), List.of(fodder.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2");
    }

    @Test
    @DisplayName("Only the first instant or sorcery each turn gets casualty")
    void onlyFirstInstantOrSorceryGetsCasualty() {
        harness.addToBattlefield(player1, new AnheloThePainter());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castWithCasualty(player1, 0, player2.getId(), List.of(fodder.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no matching casualty cost");
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(fodder.getId()));
    }
}
