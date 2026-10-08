package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VraskaTheSilencer.class, LlanowarElves.class, Shock.class})
class VraskaTheSilencerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying returns the opponent's nontoken creature as a tapped Treasure")
    void payingReturnsCreatureAsTappedTreasure() {
        harness.addToBattlefield(player1, new VraskaTheSilencer());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Llanowar Elves"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Llanowar Elves");
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(returned.getCard().getSubtypes()).containsExactly(CardSubtype.TREASURE);
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The returned Treasure can sacrifice itself for mana")
    void returnedTreasureProducesManaAndSacrifices() {
        harness.addToBattlefield(player1, new VraskaTheSilencer());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Llanowar Elves"));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Llanowar Elves");
        returned.untap();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(returned), 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(returned);
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("The returned Treasure retains its original tap ability")
    void returnedTreasureRetainsOriginalManaAbility() {
        harness.addToBattlefield(player1, new VraskaTheSilencer());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Llanowar Elves"));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Llanowar Elves");
        returned.untap();
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(returned));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(returned);
    }

    @Test
    @DisplayName("Declining leaves the creature in its owner's graveyard and spends no mana")
    void decliningDoesNotReturnCreatureOrSpendMana() {
        harness.addToBattlefield(player1, new VraskaTheSilencer());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Llanowar Elves"));
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature owned by Vraska's controller still returns when it dies under opposing control")
    void returnsOwnCreatureThatDiedUnderOpponentControl() {
        harness.addToBattlefield(player1, new VraskaTheSilencer());
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        gd.stolenCreatures.put(stolen.getId(), player1.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, stolen.getId());
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Llanowar Elves");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("A creature dying under Vraska's controller does not trigger her ability")
    void ownControlledCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new VraskaTheSilencer());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Llanowar Elves"));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
