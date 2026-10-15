package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.c.CruxOfFate;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.Fling;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindSpring;
import com.github.laxika.magicalvibes.cards.o.OjutaisCommand;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SupplyDemand;
import com.github.laxika.magicalvibes.cards.w.WearTear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    @Test
    @CardUsed({WearTear.class, FountainOfYouth.class, AngelicChorus.class})
    @DisplayName("A split card is cast as one half it can pay for, paying only that half's cost")
    void castsSplitCardAsOneHalfForItsCost() {
        WearTear wearTear = new WearTear();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(wearTear)));
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");

        castWish();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        // Tear's {W} can't be paid, and fuse applies only from a hand (CR 702.102a).
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Wear — Destroy target artifact");
        harness.handleListChoice(player1, "Wear — Destroy target artifact");
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(wearTear.getId()));
        harness.handlePermanentChosen(player1, fountainId);

        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Wear");
        assertThat(gd.stack.getLast().getSourceZone()).isEqualTo(Zone.OUTSIDE_GAME);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    @CardUsed({WearTear.class, FountainOfYouth.class})
    @DisplayName("Only a half whose cost can be paid is offered, and with none the card stays outside the game")
    void unpayableSplitHalvesAreNotOffered() {
        WearTear wearTear = new WearTear();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(wearTear)));
        harness.addToBattlefield(player2, new FountainOfYouth());

        castWish();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(wearTear);
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }
    @Test
    @CardUsed(SupplyDemand.class)
    @DisplayName("A paid split half with {X} announces X before it is cast, and pays that X")
    void paidSplitHalfAnnouncesX() {
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(new SupplyDemand())));

        castWish();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        // Demand needs {U}, so only Supply can be paid for (CR 601.2h).
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Supply — Create X 1/1 green Saproling creature tokens");
        harness.handleListChoice(player1, "Supply — Create X 1/1 green Saproling creature tokens");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("0", "1", "2");
        harness.handleListChoice(player1, "2");

        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Supply");
        assertThat(gd.stack.getLast().getXValue()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
    }
}
