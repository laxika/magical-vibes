package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.Endurance;
import com.github.laxika.magicalvibes.cards.i.IncrementalBlight;
import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WickersmithsTools.class, Endurance.class, IncrementalBlight.class, Skinrender.class})
class WickersmithsToolsTest extends BaseCardTest {

    @Test
    void putsOneChargeCounterOnOneOrMoreMinusOneMinusOneCounters() {
        Permanent tools = harness.addToBattlefieldAndReturn(player1, new WickersmithsTools());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Endurance());
        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(tools.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    void addsManaOfChosenColor() {
        harness.addToBattlefield(player1, new WickersmithsTools());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void createsTappedScarecrowArtifactCreaturesEqualToChargeCounters() {
        Permanent tools = harness.addToBattlefieldAndReturn(player1, new WickersmithsTools());
        tools.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wickersmith's Tools");
        harness.assertInGraveyard(player1, "Wickersmith's Tools");
        List<Permanent> tokens = findPermanents(player1, "Scarecrow");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.getCard().getColors()).isEmpty();
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SCARECROW);
        });
    }

    @Test
    void separateCounterPlacementsEachAddOneChargeCounter() {
        Permanent tools = harness.addToBattlefieldAndReturn(player1, new WickersmithsTools());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Endurance());
        harness.setHand(player1, List.of(new Skinrender(), new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castCreature(player1, 0, creature.getId());
        resolveAllTriggers();
        assertThat(tools.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        harness.castCreature(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(tools.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Endurance");
        harness.assertInGraveyard(player2, "Endurance");
    }

    @Test
    void triggersWhenOpponentPutsCountersOnTheirOwnCreatureThatDies() {
        Permanent tools = harness.addToBattlefieldAndReturn(player2, new WickersmithsTools());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Skinrender());
        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(tools.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        harness.assertInGraveyard(player1, "Skinrender");
    }

    @Test
    void zeroChargeCountersStillPaysCostButCreatesNoTokens() {
        harness.addToBattlefield(player1, new WickersmithsTools());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Wickersmith's Tools");
        harness.assertInGraveyard(player1, "Wickersmith's Tools");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Scarecrow")).isEmpty();
    }

    @Test
    void addsOneChargeCounterForEachCreatureReceivingCountersFromOneSpell() {
        Permanent tools = harness.addToBattlefieldAndReturn(player1, new WickersmithsTools());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Endurance());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Endurance());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new Endurance());
        harness.setHand(player1, List.of(new IncrementalBlight()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId(), third.getId()));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(third.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(tools.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }
}
