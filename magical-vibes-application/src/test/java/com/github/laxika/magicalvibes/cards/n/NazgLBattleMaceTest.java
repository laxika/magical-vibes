package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
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

@CardUsed({NazgLBattleMace.class, GrizzlyBears.class, KuldothaRebirth.class, Spellbook.class})
class NazgLBattleMaceTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has menace, deathtouch, and annihilator 1")
    void equippedCreatureGetsKeywordsAndAnnihilator() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new NazgLBattleMace());
        mace.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();

        harness.addToBattlefield(player2, new GrizzlyBears());
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent may pay 3 life to keep a sacrificed nontoken permanent")
    void opponentMayPayToKeepSacrificedPermanent() {
        harness.addToBattlefield(player1, new NazgLBattleMace());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        preparePlayerTwoSorcery();
        harness.setHand(player2, List.of(new KuldothaRebirth()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player2, 0, spellbook.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Spellbook");
        harness.assertOnBattlefield(player1, "Nazgûl Battle-Mace");
    }

    @Test
    @DisplayName("An opponent declining to pay gives the sacrificed permanent to the mace's controller")
    void opponentDecliningPaymentStealsSacrificedPermanent() {
        harness.addToBattlefield(player1, new NazgLBattleMace());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        preparePlayerTwoSorcery();
        harness.setHand(player2, List.of(new KuldothaRebirth()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player2, 0, spellbook.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Spellbook");
        harness.assertNotInGraveyard(player2, "Spellbook");
    }

    private void preparePlayerTwoSorcery() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
