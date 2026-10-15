package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BronzeSword;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.cards.f.FinalFlare;
import com.github.laxika.magicalvibes.cards.o.OmenOfTheSun;
import com.github.laxika.magicalvibes.cards.s.SetessanTraining;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreamshaperShaman.class, Forest.class, NyxbornColossus.class, BronzeSword.class, FinalFlare.class, SetessanTraining.class, OmenOfTheSun.class})
class DreamshaperShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Pays and sacrifices a nonland permanent, then puts the first revealed nonland permanent onto the battlefield")
    void paysSacrificesAndPutsFirstNonlandPermanentOntoBattlefield() {
        harness.addToBattlefield(player1, new DreamshaperShaman());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());
        harness.setLibrary(player1, List.of(new Forest(), new FinalFlare(), new BronzeSword()));

        resolveEndStepTrigger();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bronze Sword");
        harness.assertNotOnBattlefield(player1, "Nyxborn Colossus");
        harness.assertInGraveyard(player1, "Nyxborn Colossus");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Final Flare");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Declining leaves the battlefield and library unchanged")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new DreamshaperShaman());
        harness.addToBattlefield(player1, new NyxbornColossus());
        harness.setLibrary(player1, List.of(new Forest(), new FinalFlare(), new BronzeSword()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Nyxborn Colossus");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Final Flare", "Bronze Sword");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Sacrifice and payment still happen when the library has no nonland permanent")
    void noNonlandPermanentIsFound() {
        harness.addToBattlefield(player1, new DreamshaperShaman());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());
        harness.setLibrary(player1, List.of(new Forest(), new FinalFlare()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nyxborn Colossus");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Final Flare");
    }

    @Test
    @DisplayName("The Shaman can sacrifice itself to pay for its ability")
    void canSacrificeItself() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new DreamshaperShaman());
        harness.setLibrary(player1, List.of(new NyxbornColossus()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, shaman.getId());

        harness.assertInGraveyard(player1, "Dreamshaper Shaman");
        harness.assertNotOnBattlefield(player1, "Dreamshaper Shaman");
        harness.assertOnBattlefield(player1, "Nyxborn Colossus");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Unrevealed cards stay above the randomized revealed cards")
    void preservesUnrevealedLibraryOrder() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new DreamshaperShaman());
        harness.setLibrary(player1, List.of(
                new Forest(), new FinalFlare(), new BronzeSword(),
                new NyxbornColossus(), new DreamshaperShaman()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, shaman.getId());

        harness.assertOnBattlefield(player1, "Bronze Sword");
        harness.assertNotOnBattlefield(player1, "Nyxborn Colossus");
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .extracting(Card::getName)
                .containsExactly("Nyxborn Colossus", "Dreamshaper Shaman");
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 4))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Final Flare");
    }

    @Test
    @DisplayName("An empty library still permits payment and sacrifice")
    void emptyLibraryStillPaysAndSacrifices() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new DreamshaperShaman());
        harness.setLibrary(player1, List.of());

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, shaman.getId());

        harness.assertInGraveyard(player1, "Dreamshaper Shaman");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Lands and opposing permanents cannot be sacrificed")
    void sacrificeChoiceOnlyIncludesControlledNonlandPermanents() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new DreamshaperShaman());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new BronzeSword());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new NyxbornColossus());
        harness.setLibrary(player1, List.of(new Forest()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactlyInAnyOrder(shaman.getId(), sword.getId());
        harness.handlePermanentChosen(player1, sword.getId());
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Nyxborn Colossus");
    }

    @Test
    @DisplayName("The ability does not trigger during the opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new DreamshaperShaman());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A revealed Aura stays in the library when no legal creature remains")
    void auraWithoutLegalAttachmentStaysInLibrary() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new DreamshaperShaman());
        harness.setLibrary(player1, List.of(new Forest(), new SetessanTraining(), new FinalFlare()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.END_STEP,
                () -> harness.handlePermanentChosen(player1, shaman.getId()));

        harness.assertNotOnBattlefield(player1, "Setessan Training");
        harness.assertNotInGraveyard(player1, "Setessan Training");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Final Flare");
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Setessan Training", "Forest");
    }

    @Test
    @DisplayName("A revealed Aura offers a legal attachment choice before entering")
    void revealedAuraCanEnchantRemainingCreature() {
        harness.addToBattlefield(player1, new DreamshaperShaman());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new BronzeSword());
        harness.setLibrary(player1, List.of(new SetessanTraining(), new Forest(), new FinalFlare()));

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.END_STEP,
                () -> harness.handlePermanentChosen(player1, sword.getId()));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof SetessanTraining)
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getAttachedTo()).isEqualTo(creature.getId()));
    }

    @Test
    @DisplayName("A revealed noncreature permanent triggers its enters ability")
    void revealedEnchantmentTriggersItsEntersAbility() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new DreamshaperShaman());
        harness.setLibrary(player1, List.of(new OmenOfTheSun(), new Forest()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        resolveEndStepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.handlePermanentChosen(player1, shaman.getId());
            harness.passBothPriorities();
        });

        harness.assertOnBattlefield(player1, "Omen of the Sun");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }
}
