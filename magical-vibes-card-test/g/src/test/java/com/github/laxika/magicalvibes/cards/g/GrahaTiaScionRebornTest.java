package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrahaTiaScionReborn.class, DarkRitual.class, Divination.class, SolRing.class})
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
    void payingLifePreventsFurtherTriggersThatTurn() {
        harness.addToBattlefield(player1, new GrahaTiaScionReborn());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Hero")).isEqualTo(1);
        harness.assertLife(player1, 19);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void decliningAllowsPaymentForALaterNoncreatureSpell() {
        harness.addToBattlefield(player1, new GrahaTiaScionReborn());
        harness.setHand(player1, List.of(new SolRing(), new SolRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Hero")).isEqualTo(1);
        assertThat(findPermanent(player1, "Hero").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 19);
    }

    @Test
    void creatureSpellDoesNotTriggerOrConsumeTheOpportunity() {
        harness.setHand(player1, List.of(new GrahaTiaScionReborn(), new SolRing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Hero")).isEqualTo(1);
        harness.assertLife(player1, 19);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GrahaTiaScionReborn());
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(countPermanents(player1, "Hero")).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotCreateHeroWhenLifeIsLessThanThePayment() {
        harness.addToBattlefield(player1, new GrahaTiaScionReborn());
        harness.setLife(player1, 2);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Hero")).isZero();
        harness.assertLife(player1, 2);
    }

    @Test
    void combatDamageGainsLifeThroughLifelink() {
        addCreatureReady(player1, new GrahaTiaScionReborn());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
