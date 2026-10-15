package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SavageSummoning;
import com.github.laxika.magicalvibes.cards.f.FreshFacedRecruit;
import com.github.laxika.magicalvibes.cards.h.HitchclawRecluse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuildmagesForum.class, FreshFacedRecruit.class, HitchclawRecluse.class, SavageSummoning.class})
class GuildmagesForumTest extends BaseCardTest {

    @Test
    void manaCounterStacksWithSavageSummoningCounter() {
        harness.addToBattlefield(player1, new GuildmagesForum());
        harness.setHand(player1, List.of(new SavageSummoning()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");
        FreshFacedRecruit creature = new FreshFacedRecruit();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(creature));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, creature.getName())
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void manaFromTwoForumsGivesTwoCounters() {
        harness.addToBattlefield(player1, new GuildmagesForum());
        harness.addToBattlefield(player1, new GuildmagesForum());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");
        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.setHand(player1, List.of(new FreshFacedRecruit()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fresh-Faced Recruit")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void forumManaCanPayGenericPartOfMulticoloredCreatureCost() {
        harness.addToBattlefield(player1, new GuildmagesForum());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new FreshFacedRecruit()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fresh-Faced Recruit")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void spendingForumManaOnAnInstantDoesNotGrantALaterCreatureACounter() {
        harness.addToBattlefield(player1, new GuildmagesForum());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.setHand(player1, List.of(new SavageSummoning()));
        harness.castAndResolveInstant(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new FreshFacedRecruit()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fresh-Faced Recruit")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void unspentForumManaDoesNotGrantACounter() {
        harness.addToBattlefield(player1, new GuildmagesForum());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new FreshFacedRecruit()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fresh-Faced Recruit")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The first ability adds a colorless mana")
    void addsColorlessMana() {
        harness.addToBattlefield(player1, new GuildmagesForum());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability gives a multicolored creature an additional counter")
    void multicoloredCreatureEntersWithAdditionalCounter() {
        harness.addToBattlefield(player1, new GuildmagesForum());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        FreshFacedRecruit creature = new FreshFacedRecruit();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(creature));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, creature.getName());
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter rider does not apply to a monocolored creature")
    void monocoloredCreatureDoesNotGetAdditionalCounter() {
        harness.addToBattlefield(player1, new GuildmagesForum());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new HitchclawRecluse()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Hitchclaw Recluse")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

}
