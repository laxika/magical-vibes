package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArchfiendOfDepravity;
import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.d.DivineReckoning;
import com.github.laxika.magicalvibes.cards.d.Duneblast;
import com.github.laxika.magicalvibes.cards.i.InnocentBlood;
import com.github.laxika.magicalvibes.cards.n.Nevermore;
import com.github.laxika.magicalvibes.cards.p.PathOfPeace;
import com.github.laxika.magicalvibes.cards.p.PromiseOfLoyalty;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlimpseTheUnthinkablePlaytest.class, PathOfPeace.class, DivineReckoning.class, Nevermore.class,
        DiabolicEdict.class, InnocentBlood.class, GrizzlyBears.class, PromiseOfLoyalty.class, Duneblast.class,
        ArchfiendOfDepravity.class})
class GlimpseTheUnthinkablePlaytestTest extends BaseCardTest {

    @Test
    @DisplayName("Shroud prevents spells from targeting Glimpse, the Unthinkable")
    void shroudPreventsTargeting() {
        Permanent glimpse = harness.addToBattlefieldAndReturn(
                player1, new GlimpseTheUnthinkablePlaytest());

        harness.setHand(player2, List.of(new PathOfPeace()));
        harness.addMana(player2, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, glimpse.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Divine Reckoning cannot keep Glimpse even when it is the only creature")
    void cannotBeChosenToSurviveDivineReckoning() {
        harness.addToBattlefield(player1, new GlimpseTheUnthinkablePlaytest());

        harness.castFromHand(player1, new DivineReckoning(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glimpse, the Unthinkable");
        harness.assertInGraveyard(player1, "Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("Glimpse's name cannot be chosen while it is on the battlefield")
    void cannotChooseNameWhileOnBattlefield() {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());

        harness.castFromHand(player1, new Nevermore(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Glimpse, the Unthinkable"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.assertNotOnBattlefield(player1, "Nevermore");
    }

    @Test
    @DisplayName("Glimpse's name can be chosen while it is only in a player's hand")
    void canChooseNameWhileOffBattlefield() {
        harness.setHand(player2, List.of(new GlimpseTheUnthinkablePlaytest()));

        harness.castFromHand(player1, new Nevermore(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Glimpse, the Unthinkable");

        harness.assertOnBattlefield(player1, "Nevermore");
        assertThat(findPermanent(player1, "Nevermore").getChosenName())
                .isEqualTo("Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("An edict can't make its target sacrifice Glimpse when it is their only creature")
    void edictCannotChooseGlimpseAsOnlyCreature() {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());

        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Glimpse, the Unthinkable");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An edict takes the other creature without a prompt when Glimpse is the alternative")
    void edictTakesOtherCreatureAutomatically() {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Glimpse, the Unthinkable");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Each-player sacrifice skips a player whose only creature is Glimpse")
    void eachPlayerSacrificeSkipsGlimpse() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());

        harness.castFromHand(player1, new InnocentBlood(), "{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("Card-name suggestions leave out Glimpse's name while it is on the battlefield")
    void nameSuggestionsOmitGlimpseOnBattlefield() throws Exception {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.clearMessages();

        harness.castFromHand(player1, new Nevermore(), "{1}{W}{W}");
        harness.passBothPriorities();

        var prompts = harness.getConn1().getMessagesContaining("LIST_PICK");
        assertThat(prompts).isNotEmpty();
        var options = new JacksonConfig().objectMapper().readTree(prompts.getLast()).get("options");
        List<String> names = new java.util.ArrayList<>();
        options.forEach(option -> names.add(option.asString()));
        assertThat(names).contains("Grizzly Bears").doesNotContain("Glimpse, the Unthinkable");

        harness.handleListChoice(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A keep-one-then-sacrifice-the-rest effect sacrifices Glimpse when it is the only creature")
    void keepOneEffectSacrificesGlimpseAsOnlyCreature() {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());

        castPromiseOfLoyalty();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player2, "Glimpse, the Unthinkable");
        harness.assertInGraveyard(player2, "Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("A keep-one effect keeps the other creature without a prompt and sacrifices Glimpse")
    void keepOneEffectKeepsOtherCreatureAutomatically() {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castPromiseOfLoyalty();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bears);
        assertThat(bears.getCounterCount(CounterType.VOW)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("Choose-up-to-one-then-destroy-the-rest destroys Glimpse without a prompt when it is the only creature")
    void chooseUpToOneDestroyRestDestroysGlimpse() {
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());

        harness.castFromHand(player1, new Duneblast(), "{4}{W}{B}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Glimpse, the Unthinkable");
    }

    @Test
    @DisplayName("Keeping up to two creatures can't keep Glimpse, so it is sacrificed with the rest")
    void keepUpToTwoCannotKeepGlimpse() {
        harness.addToBattlefield(player1, new ArchfiendOfDepravity());
        Permanent glimpse = harness.addToBattlefieldAndReturn(player2, new GlimpseTheUnthinkablePlaytest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player2);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(glimpse.getCard());
    }

    @Test
    @DisplayName("Keeping up to two creatures sacrifices Glimpse without a prompt when it is the only creature")
    void keepUpToTwoSacrificesGlimpseAsOnlyCreature() {
        harness.addToBattlefield(player1, new ArchfiendOfDepravity());
        harness.addToBattlefield(player2, new GlimpseTheUnthinkablePlaytest());

        advanceToEndStep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Glimpse, the Unthinkable");
    }

    private void castPromiseOfLoyalty() {
        harness.setHand(player1, List.of(new PromiseOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
