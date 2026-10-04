package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PestSummoning;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeralAppetite.class, PestSummoning.class, GrizzlyBears.class, Cancel.class, Shock.class})
class FeralAppetiteTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Pests get +1/+0 and deathtouch")
    void attackingPestsGetBoostAndDeathtouch() {
        harness.addToBattlefield(player1, new FeralAppetite());
        harness.setHand(player1, List.of(new PestSummoning()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> pests = findPermanents(player1, "Pest");
        pests.forEach(pest -> pest.setSummoningSick(false));
        pests.getFirst().setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, pests.getFirst())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, pests.getFirst())).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, pests.getFirst(), Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, pests.get(1))).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, pests.get(1), Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Exiling a creature card creates a Pest token")
    void exilingCreatureCreatesPest() {
        harness.addToBattlefield(player1, new FeralAppetite());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        assertThat(findPermanents(player1, "Pest"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Exiling a noncreature card creates no Pest")
    void exilingNoncreatureCreatesNoPest() {
        harness.addToBattlefield(player1, new FeralAppetite());
        Card noncreature = new Cancel();
        harness.setGraveyard(player2, List.of(noncreature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, noncreature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Cancel");
        assertThat(findPermanents(player1, "Pest"))
                .isEmpty();
    }

    @Test
    @DisplayName("The created Pest token gains 1 life when it dies")
    void createdPestGainsLifeWhenItDies() {
        harness.addToBattlefield(player1, new FeralAppetite());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent pest = findPermanent(player1, "Pest");
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, pest.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("A creature in your own graveyard can create a Pest")
    void exilingOwnCreatureCreatesPest() {
        harness.addToBattlefield(player1, new FeralAppetite());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertThat(findPermanents(player1, "Pest")).hasSize(1);
        assertThat(findPermanents(player2, "Pest")).isEmpty();
    }

    @Test
    @DisplayName("Two activations targeting the same card create only one Pest")
    void missingGraveyardTargetCreatesNoAdditionalPest() {
        harness.addToBattlefield(player1, new FeralAppetite());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(findPermanents(player1, "Pest")).hasSize(1);
    }

    @Test
    @DisplayName("The bonus excludes opposing Pests and attacking non-Pests")
    void bonusOnlyAppliesToYourAttackingPests() {
        harness.addToBattlefield(player1, new FeralAppetite());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new PestSummoning()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player2, 0, 0);
        Permanent opposingPest = findPermanent(player2, "Pest");
        opposingPest.setSummoningSick(false);
        opposingPest.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, opposingPest)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opposingPest, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Multiple copies stack their power bonus only while a Pest attacks")
    void multipleCopiesStopBoostingWhenPestStopsAttacking() {
        harness.addToBattlefield(player1, new FeralAppetite());
        harness.addToBattlefield(player1, new FeralAppetite());
        harness.setHand(player1, List.of(new PestSummoning()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent pest = findPermanent(player1, "Pest");
        pest.setSummoningSick(false);
        pest.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, pest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pest)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, pest, Keyword.DEATHTOUCH)).isTrue();

        pest.setAttacking(false);

        assertThat(gqs.getEffectivePower(gd, pest)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, pest, Keyword.DEATHTOUCH)).isFalse();
    }
}
