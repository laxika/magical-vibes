package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GerminationPracticum;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.t.TemporalMastery;
import com.github.laxika.magicalvibes.cards.w.WearTear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoreholdTheHistorian.class, GrizzlyBears.class, Opt.class})
class LoreholdTheHistorianTest extends BaseCardTest {

    @Test
    @DisplayName("The first drawn instant is offered for miracle {2}")
    void grantsMiracleToDrawnInstant() {
        harness.addToBattlefield(player1, new LoreholdTheHistorian());
        harness.setLibrary(player1, List.of(new Opt()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        PendingInteraction.MayAbilityChoice choice =
                (PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction();
        assertThat(choice.manaCost()).isEqualTo("{2}");
    }

    @Test
    @DisplayName("The granted miracle cost remains available after Lorehold leaves the battlefield")
    void snapshotsGrantedMiracleCost() {
        Permanent lorehold = harness.addToBattlefieldAndReturn(player1, new LoreholdTheHistorian());
        Opt opt = new Opt();
        harness.setLibrary(player1, List.of(opt));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        gd.playerBattlefields.get(player1.getId()).remove(lorehold);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        PendingInteraction.MayAbilityChoice choice =
                (PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction();
        assertThat(choice.manaCost()).isEqualTo("{2}");
    }

    @Test
    @DisplayName("A drawn creature is not offered the granted miracle")
    void doesNotGrantMiracleToCreatures() {
        harness.addToBattlefield(player1, new LoreholdTheHistorian());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent upkeep offers a discard followed by a draw")
    void opponentUpkeepOffersDiscardAndDraw() {
        harness.addToBattlefield(player1, new LoreholdTheHistorian());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @CardUsed(GerminationPracticum.class)
    @DisplayName("A drawn sorcery can be cast for two generic mana")
    void castsDrawnSorceryForGrantedMiracleCost() {
        Permanent lorehold = harness.addToBattlefieldAndReturn(player1, new LoreholdTheHistorian());
        harness.setLibrary(player1, List.of(new GerminationPracticum()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Germination Practicum");
        assertThat(lorehold.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @CardUsed({WearTear.class, FountainOfYouth.class, AngelicChorus.class})
    @DisplayName("A drawn split card is cast as the chosen half for the granted miracle cost")
    void castsDrawnSplitCardHalfForGrantedMiracleCost() {
        harness.addToBattlefield(player1, new LoreholdTheHistorian());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");
        WearTear wearTear = new WearTear();
        harness.setLibrary(player1, List.of(wearTear));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Cast from hand, so fuse is offered too (CR 702.102a).
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Wear — Destroy target artifact", "Tear — Destroy target enchantment",
                        "Fuse — Wear and then Tear");
        harness.handleListChoice(player1, "Wear — Destroy target artifact");
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(wearTear.getId()));
        harness.handlePermanentChosen(player1, fountainId);

        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Wear");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("A second drawn instant is not offered miracle")
    void doesNotGrantMiracleToSecondDraw() {
        harness.addToBattlefield(player1, new LoreholdTheHistorian());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Opt()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Opt");
    }

    @Test
    @DisplayName("An opponent's drawn instant does not gain miracle")
    void doesNotGrantMiracleToOpponent() {
        harness.addToBattlefield(player1, new LoreholdTheHistorian());
        harness.setLibrary(player2, List.of(new Opt()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the upkeep discard leaves the hand and library unchanged")
    void canDeclineUpkeepDiscard() {
        harness.addToBattlefield(player1, new LoreholdTheHistorian());
        GrizzlyBears card = new GrizzlyBears();
        Opt top = new Opt();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(top));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An empty hand cannot discard and therefore does not draw")
    void emptyHandDoesNotDrawAtOpponentUpkeep() {
        harness.addToBattlefield(player1, new LoreholdTheHistorian());
        harness.setHand(player1, List.of());
        Opt top = new Opt();
        harness.setLibrary(player1, List.of(top));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("Lorehold does not trigger during its controller's upkeep")
    void doesNotTriggerOnOwnUpkeep() {
        harness.addToBattlefield(player1, new LoreholdTheHistorian());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed(TemporalMastery.class)
    @DisplayName("A card with native miracle also offers Lorehold's granted miracle cost")
    void offersGrantedMiracleAlongsideNativeMiracle() {
        harness.addToBattlefield(player1, new LoreholdTheHistorian());
        harness.setLibrary(player1, List.of(new TemporalMastery()));
        List<String> offeredCosts = new ArrayList<>();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        for (int decisions = 0; decisions < 10; decisions++) {
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice choice) {
                if (choice.manaCost() == null) {
                    harness.handleMayAbilityChosen(player1, true);
                } else {
                    offeredCosts.add(choice.manaCost());
                    harness.handleMayAbilityChosen(player1, false);
                }
            } else if (!gd.pendingMayAbilities.isEmpty()) {
                harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
            } else if (!gd.stack.isEmpty()) {
                harness.passBothPriorities();
            } else {
                break;
            }
        }

        assertThat(offeredCosts).contains("{1}{U}", "{2}");
        harness.assertInHand(player1, "Temporal Mastery");
    }
}
