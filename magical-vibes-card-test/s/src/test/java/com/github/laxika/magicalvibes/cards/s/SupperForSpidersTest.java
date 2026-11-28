package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.MasterfulReplication;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SupperForSpiders.class, GrizzlyBears.class, LlanowarElves.class, LightningBolt.class})
class SupperForSpidersTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an opponent's creature card that died this turn as a Food artifact, not one that didn't")
    void returnsEligibleOpponentCreaturesAsFoodArtifacts() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Llanowar Elves"));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SupperForSpiders()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        Permanent returned = findPermanent(player1, "Llanowar Elves");
        assertThat(gqs.isArtifact(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.FOOD)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.ELF)).isFalse();
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The Food keeps its own abilities and can be sacrificed for 3 life, going to its owner's graveyard")
    void retainsOriginalAbilitiesAndCanBeSacrificedForFood() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Llanowar Elves"));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SupperForSpiders()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        Permanent returned = findPermanent(player1, "Llanowar Elves");
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(returned));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        returned.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(returned), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(findPermanents(player1, "Llanowar Elves")).isEmpty();
        assertThat(findPermanents(player2, "Llanowar Elves")).isEmpty();
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Returns every eligible opponent card and none from your own graveyard")
    void returnsEveryEligibleOpponentCardButLeavesYourOwnGraveyardAlone() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Llanowar Elves"));
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Llanowar Elves"));

        harness.setHand(player1, List.of(new SupperForSpiders()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanents(player1, "Llanowar Elves")).hasSize(1);
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(permanent -> assertThat(gqs.isCreature(gd, permanent)).isFalse());
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotInGraveyard(player2, "Llanowar Elves");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @CardUsed(SculptingSteel.class)
    @DisplayName("A copy of the Food copies the printed creature card, not the Food types")
    void copyingReturnedFoodCopiesOriginalCreatureTypes() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.setHand(player1, List.of(new SupperForSpiders(), new SculptingSteel()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
        Permanent food = findPermanent(player1, "Grizzly Bears");

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, food.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(food.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.isCreature(gd, copy)).isTrue();
        assertThat(gqs.isArtifact(gd, copy)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, copy, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.isCreature(gd, food)).isFalse();
    }

    @Test
    @CardUsed({MasterfulReplication.class, Ornithopter.class})
    @DisplayName("A Food that becomes a copy of an artifact creature is still only a Food artifact")
    void foodThatBecomesACopyKeepsItsFoodTypes() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.setHand(player1, List.of(new SupperForSpiders(), new MasterfulReplication()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
        Permanent food = findPermanent(player1, "Grizzly Bears");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castInstant(player1, 0, 1, ornithopter.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveName(gd, food)).isEqualTo("Ornithopter");
        assertThat(gqs.isCreature(gd, food)).isFalse();
        assertThat(gqs.isArtifact(gd, food)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, food, CardSubtype.FOOD)).isTrue();
    }
}
