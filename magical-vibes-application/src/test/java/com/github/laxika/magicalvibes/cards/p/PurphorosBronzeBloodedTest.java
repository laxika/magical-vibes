package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IncandescentSoulstoke;
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

@CardUsed({PurphorosBronzeBlooded.class, GrizzlyBears.class, IncandescentSoulstoke.class, Ornithopter.class})
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
        harness.setHand(player1, List.of(new GrizzlyBears(), new IncandescentSoulstoke(), new Ornithopter()));
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

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    private Permanent addPurphoros() {
        return harness.addToBattlefieldAndReturn(player1, new PurphorosBronzeBlooded());
    }

    private Permanent addReadyPurphoros() {
        Permanent purphoros = new Permanent(new PurphorosBronzeBlooded());
        purphoros.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(purphoros);
        return purphoros;
    }

    private void addRedDevotion(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new IncandescentSoulstoke());
        }
    }

    private void giveManaForAbility() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
