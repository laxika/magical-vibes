package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.c.CruxOfFate;
import com.github.laxika.magicalvibes.cards.f.Fling;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindSpring;
import com.github.laxika.magicalvibes.cards.o.OjutaisCommand;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YourWishIsMyCommand.class, Opt.class, GrizzlyBears.class, Forest.class,
        CruxOfFate.class, MindSpring.class, Fling.class, Shock.class, OjutaisCommand.class})
class YourWishIsMyCommandTest extends BaseCardTest {

    @Test
    void offersOnlyInstantAndSorcerySideboardCards() {
        Card instant = new Opt();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(instant, creature, land)));

        castWish();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities).singleElement().satisfies(ability ->
                assertThat(ability.targetCardId()).isEqualTo(instant.getId()));
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(instant, creature, land);
    }

    @Test
    void acceptsAnInstantAndCastsItForItsNormalCost() {
        Card instant = new Opt();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(instant)));

        castWish();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
        assertThat(gd.outsideGamePlayPermissions).doesNotContain(instant.getId());
        assertThat(gd.stack.getLast().getCard()).isSameAs(instant);
        assertThat(gd.stack.getLast().getSourceZone()).isEqualTo(Zone.OUTSIDE_GAME);
    }

    @Test
    void decliningLeavesTheSideboardCardOutsideTheGame() {
        Card instant = new Opt();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(instant)));

        castWish();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(instant);
        assertThat(gd.outsideGamePlayPermissions).doesNotContain(instant.getId());
    }

    @Test
    void cannotCastAnInstantWithoutPayingItsManaCost() {
        Card instant = new Opt();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(instant)));

        castWish();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(instant);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.outsideGamePlayPermissions).doesNotContain(instant.getId());
    }

    @Test
    void canDeclineTheFirstEligibleCardAndCastOnlyTheSecond() {
        Card first = new Opt();
        Card second = new Opt();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(first, second)));

        castWish();
        harness.handleMayAbilityChosen(player1, false);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(first);
        assertThat(gd.stack).singleElement().satisfies(entry ->
                assertThat(entry.getCard()).isSameAs(second));
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void emptySideboardLeavesNoCastOffer() {
        gd.playerSideboards.put(player1.getId(), new ArrayList<>());

        castWish();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Your Wish Is My Command");
    }

    @Test
    void choosingAnAdditionalModeStillPaysManaAndRemovesTheSideboardCard() {
        Card spell = new CruxOfFate();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(spell)));

        castWish();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Destroy all Dragon creatures");
        harness.handleListChoice(player1, "Destroy all non-Dragon creatures");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).singleElement().satisfies(entry -> {
            assertThat(entry.getCard().getId()).isEqualTo(spell.getId());
            assertThat(entry.getSourceZone()).isEqualTo(Zone.OUTSIDE_GAME);
        });
    }

    @Test
    void choosingOnlyTheNormalModeStillRequiresPayingMana() {
        Card spell = new CruxOfFate();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(spell)));

        castWish();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Destroy all Dragon creatures");
        harness.handleListChoice(player1, "Done");

        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.outsideGamePlayPermissions).doesNotContain(spell.getId());
    }

    @Test
    void choosingTwoNormalModesStillPaysManaAndUsesTheSideboardZone() {
        Card spell = new OjutaisCommand();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(spell)));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);

        castWish();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "You gain 4 life");
        harness.handleListChoice(player1, "Draw a card");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).singleElement().satisfies(entry ->
                assertThat(entry.getSourceZone()).isEqualTo(Zone.OUTSIDE_GAME));
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void canChooseAndPayANonzeroXForASideboardSpell() {
        Card spell = new MindSpring();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(spell)));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        castWish();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
    }

    @Test
    void canPayAMandatoryCreatureSacrificeForASideboardSpell() {
        Card spell = new Fling();
        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(spell)));
        harness.setLife(player2, 20);

        castWish();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
    }

    @Test
    void unsuccessfulTargetedCastDoesNotGrantPermissionToCastLater() {
        Card spell = new Shock();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(spell)));

        castWish();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.outsideGamePlayPermissions).doesNotContain(spell.getId());
    }

    private void castWish() {
        harness.castFromHand(player1, new YourWishIsMyCommand(), "{1}{U}");
        harness.passBothPriorities();
    }
}
