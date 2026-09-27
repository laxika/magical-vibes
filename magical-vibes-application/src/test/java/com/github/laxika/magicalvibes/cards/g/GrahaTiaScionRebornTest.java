package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrahaTiaScionReborn.class, DarkRitual.class, Divination.class, GrizzlyBears.class})
class GrahaTiaScionRebornTest extends BaseCardTest {

    @Test
    void mayPayLifeEqualToSpellManaValueToCreateHeroWithCounters() {
        harness.addToBattlefield(player1, new GrahaTiaScionReborn());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent hero = findPermanent(player1, "Hero");
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hero.getEffectivePower()).isEqualTo(4);
        assertThat(hero.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    void mayDeclineWithoutCreatingHero() {
        harness.addToBattlefield(player1, new GrahaTiaScionReborn());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Hero")).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void triggersOnlyOnceEachTurnAndOnlyForNoncreatureSpells() {
        harness.addToBattlefield(player1, new GrahaTiaScionReborn());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);

        assertThat(countPermanents(player1, "Hero")).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
