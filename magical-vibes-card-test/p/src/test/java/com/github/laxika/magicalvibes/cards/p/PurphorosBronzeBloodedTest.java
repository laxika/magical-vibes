package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ArenaTrickster;
import com.github.laxika.magicalvibes.cards.b.BronzeSword;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OmenOfTheForge;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({PurphorosBronzeBlooded.class, GrizzlyBears.class, ArenaTrickster.class, Ornithopter.class,
        BronzeSword.class, OmenOfTheForge.class})
class PurphorosBronzeBloodedTest extends BaseCardTest {

    @Test
    @DisplayName("Purphoros is not a creature below five devotion to red")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent purphoros = addPurphoros();

        assertThat(gqs.isCreature(gd, purphoros)).isFalse();
        assertThat(gqs.isEnchantment(gd, purphoros)).isTrue();
    }

    @Test
    @DisplayName("Purphoros becomes a creature at five devotion to red")
    void becomesCreatureAtDevotionThreshold() {
        Permanent purphoros = addPurphoros();
        addRedDevotion(4);

        assertThat(gqs.isCreature(gd, purphoros)).isTrue();
    }

    @Test
    @DisplayName("Other creatures you control have haste")
    void grantsHasteToOtherCreaturesYouControl() {
        Permanent purphoros = addPurphoros();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, purphoros, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The ability offers red creature cards and artifact creature cards from hand")
    void offersRedCreaturesAndArtifactCreatures() {
        addReadyPurphoros();
        harness.setHand(player1, List.of(new GrizzlyBears(), new ArenaTrickster(), new Ornithopter()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1, 2);
    }

    @Test
    @DisplayName("A creature put onto the battlefield is sacrificed at the next end step")
    void putsArtifactCreatureOntoBattlefieldAndSacrificesIt() {
        addReadyPurphoros();
        harness.setHand(player1, List.of(new Ornithopter()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent ornithopter = findPermanent(player1, "Ornithopter");
        assertThat(gqs.hasKeyword(gd, ornithopter, Keyword.HASTE)).isTrue();

        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Purphoros loses creature status when red devotion falls below five")
    void losesCreatureStatusWhenDevotionFalls() {
        Permanent purphoros = addPurphoros();
        addRedDevotion(4);
        assertThat(gqs.isCreature(gd, purphoros)).isTrue();
        assertThat(gqs.hasKeyword(gd, purphoros, Keyword.HASTE)).isFalse();

        gd.playerBattlefields.get(player1.getId()).removeLast();

        assertThat(gqs.isCreature(gd, purphoros)).isFalse();
        assertThat(gqs.isEnchantment(gd, purphoros)).isTrue();
    }

    @Test
    @DisplayName("An opponent's red permanents do not contribute to your devotion")
    void ignoresOpponentsDevotion() {
        Permanent purphoros = addPurphoros();
        addRedDevotion(3);
        harness.addToBattlefield(player2, new ArenaTrickster());

        assertThat(gqs.isCreature(gd, purphoros)).isFalse();
    }

    @Test
    @DisplayName("You may decline to put a creature onto the battlefield")
    void mayDeclineCreatureEntry() {
        addPurphoros();
        harness.setHand(player1, List.of(new PurphorosBronzeBlooded()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Purphoros, Bronze-Blooded")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A red nonartifact creature enters untapped and gains haste from Purphoros")
    void putsRedCreatureOntoBattlefield() {
        addPurphoros();
        harness.setHand(player1, List.of(new ArenaTrickster()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent creature = findPermanent(player1, "Arena Trickster");
        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Losing Purphoros removes haste but does not cancel the delayed sacrifice")
    void sacrificePersistsAfterPurphorosLeaves() {
        Permanent purphoros = addPurphoros();
        harness.setHand(player1, List.of(new Ornithopter()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent creature = findPermanent(player1, "Ornithopter");
        gd.playerBattlefields.get(player1.getId()).remove(purphoros);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();

        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    private Permanent addPurphoros() {
        return harness.addToBattlefieldAndReturn(player1, new PurphorosBronzeBlooded());
    }

    @Test
    @DisplayName("Red noncreature cards and noncreature artifacts cannot be put onto the battlefield")
    void excludesNoncreatureCards() {
        addPurphoros();
        harness.setHand(player1, List.of(new OmenOfTheForge(), new BronzeSword(), new ArenaTrickster()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(2);
        harness.handleCardChosen(player1, 2);

        harness.assertOnBattlefield(player1, "Arena Trickster");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Activating during an end step waits until the following end step to sacrifice")
    void activationDuringEndStepWaitsForNextEndStep() {
        addPurphoros();
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.passUntil(TurnStep.END_STEP);
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gd.stack).isEmpty();

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("The delayed ability cannot sacrifice a creature now controlled by an opponent")
    void cannotSacrificeCreatureControlledByOpponent() {
        addPurphoros();
        harness.setHand(player1, List.of(new Ornithopter()));
        giveManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent creature = findPermanent(player1, "Ornithopter");
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    private Permanent addReadyPurphoros() {
        return addCreatureReady(player1, new PurphorosBronzeBlooded());
    }

    private void addRedDevotion(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new ArenaTrickster());
        }
    }

    private void giveManaForAbility() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
