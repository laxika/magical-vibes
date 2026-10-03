package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DemonBolt;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RighteousValkyrie;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AscentOfTheWorthy.class, GrizzlyBears.class, Shock.class,
        AxgardCavalry.class, DemonBolt.class, RighteousValkyrie.class})
class AscentOfTheWorthyTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I redirects damage from your other creatures to the chosen creature")
    void chapterIRedirectsDamageToChosenCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AscentOfTheWorthy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent chosen = findPermanents(player1, "Grizzly Bears").getFirst();
        Permanent damagedCreature = findPermanents(player1, "Grizzly Bears").get(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(chosen.getId(), damagedCreature.getId());
        harness.handlePermanentChosen(player1, chosen.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, damagedCreature.getId());

        assertThat(damagedCreature.getMarkedDamage()).isZero();
        assertThat(chosen.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.creatureControllerDamageRedirectShields).hasSize(1);
    }

    @Test
    @DisplayName("Chapter III returns a creature with a flying counter and Angel Warrior subtypes")
    void chapterIIIReturnsCreatureAsAngelWarrior() {
        harness.addToBattlefield(player1, new AscentOfTheWorthy());
        Permanent saga = findPermanent(player1, "Ascent of the Worthy");
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(gd.playerGraveyards.get(player1.getId()).getFirst().getId());

        harness.handleMultipleCardsChosen(player1, List.of(choice.validCardIds().getFirst()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
        assertThat(returned.getGrantedSubtypes())
                .contains(CardSubtype.ANGEL, CardSubtype.WARRIOR);
    }

    @Test
    @DisplayName("Damage redirection expires when the controller's next turn begins")
    void redirectionExpiresAtControllersNextTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AscentOfTheWorthy());
        Permanent saga = findPermanent(player1, "Ascent of the Worthy");
        saga.setCounterCount(CounterType.LORE, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent chosen = findPermanents(player1, "Grizzly Bears").getFirst();
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gd.creatureControllerDamageRedirectShields).hasSize(1);
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setHand(player1, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.creatureControllerDamageRedirectShields).hasSize(1);
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gd.creatureControllerDamageRedirectShields).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Chapters I and II choose a creature only on resolution, after responses")
    void creatureIsChosenAfterResponses(int initialLore) {
        harness.addToBattlefield(player1, new AxgardCavalry());
        harness.addToBattlefield(player1, new AxgardCavalry());
        harness.addToBattlefield(player1, new AscentOfTheWorthy());
        findPermanent(player1, "Ascent of the Worthy").setCounterCount(CounterType.LORE, initialLore);
        Permanent removed = findPermanents(player1, "Axgard Cavalry").getFirst();
        Permanent remaining = findPermanents(player1, "Axgard Cavalry").get(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.setHand(player2, List.of(new DemonBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, removed.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(removed);

        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(remaining.getId());
        harness.handlePermanentChosen(player1, remaining.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.addToBattlefield(player1, new AxgardCavalry());
        Permanent newcomer = findPermanents(player1, "Axgard Cavalry").get(1);
        harness.setHand(player2, List.of(new DemonBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, newcomer.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(newcomer).doesNotContain(remaining);
        assertThat(newcomer.getMarkedDamage()).isZero();

        harness.setHand(player2, List.of(new DemonBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, newcomer.getId());
        harness.assertNotOnBattlefield(player1, "Axgard Cavalry");
    }

    @Test
    @DisplayName("Chapter III returns a non-Angel as an Angel for entry triggers")
    void returnedCreatureTriggersAngelEntryAbility() {
        harness.addToBattlefield(player1, new RighteousValkyrie());
        harness.addToBattlefield(player1, new AscentOfTheWorthy());
        findPermanent(player1, "Ascent of the Worthy").setCounterCount(CounterType.LORE, 2);
        harness.setGraveyard(player1, List.of(new AxgardCavalry()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(choice.validCardIds().getFirst()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Axgard Cavalry");
        assertThat(returned.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertNotOnBattlefield(player1, "Ascent of the Worthy");
        harness.assertInGraveyard(player1, "Ascent of the Worthy");
    }
}
