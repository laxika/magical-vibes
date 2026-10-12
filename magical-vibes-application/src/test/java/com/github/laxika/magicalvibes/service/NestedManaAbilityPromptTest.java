package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.f.FertileGround;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LoseFocus;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.cards.p.Propaganda;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A mana ability activated while another prompt asks for a mana payment (CR 605.3a) answers its
 * own choices first, then the payment prompt returns under its original decision identity.
 */
@CardUsed({OrnithopterOfParadise.class, LoseFocus.class, BirdsOfParadise.class, GrizzlyBears.class,
        Propaganda.class, Forest.class, FertileGround.class})
class NestedManaAbilityPromptTest extends BaseCardTest {

    @Test
    @DisplayName("Mana chosen in a nested prompt pays the may-pay cost that was waiting for it")
    void nestedManaPaysCounterspellTax() {
        addCreatureReady(player1, new OrnithopterOfParadise());
        castSpellAndFaceLoseFocus(1);
        UUID payDecision = gd.interaction.activeDecisionId();

        gs.activateAbility(gd, player1, 0, 0, null, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeDecisionId()).isNotEqualTo(payDecision);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeDecisionId()).isEqualTo(payDecision);
        assertThat(gd.interaction.hasSuspendedInteraction()).isFalse();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotInGraveyard(player1, "Ornithopter of Paradise");
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Ornithopter of Paradise"))
                .hasSize(2);
    }

    @Test
    @DisplayName("A land's any-color mana trigger nests when the land is tapped during a may-pay prompt")
    void landTapTriggerNestsDuringMayPay() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FertileGround());
        aura.setAttachedTo(forest.getId());
        castSpellAndFaceLoseFocus(1);
        UUID payDecision = gd.interaction.activeDecisionId();

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeDecisionId()).isEqualTo(payDecision);
    }

    @Test
    @DisplayName("A mana ability's color prompt during attacker declaration returns to the declaration")
    void nestedManaPaysAttackTax() {
        harness.addToBattlefield(player1, new Propaganda());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new BirdsOfParadise());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        UUID declarationDecision = gd.interaction.activeDecisionId();

        gs.activateAbility(gd, player2, 1, 0, null, null, null);
        harness.handleListChoice(player2, "GREEN");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.AttackerDeclaration.class);
        assertThat(gd.interaction.activeDecisionId()).isEqualTo(declarationDecision);

        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    /**
     * Player 1 casts an Ornithopter of Paradise and player 2 answers with Lose Focus, leaving
     * player 1 at the "pay {2}?" prompt with {@code floatingMana} colorless mana in their pool.
     */
    private void castSpellAndFaceLoseFocus(int floatingMana) {
        OrnithopterOfParadise spell = new OrnithopterOfParadise();
        harness.castFromHand(player1, spell, "{2}");
        harness.addMana(player1, ManaColor.COLORLESS, floatingMana);
        harness.setHand(player2, List.of(new LoseFocus()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstantWithRepeatedCosts(player2, 0, spell.getId(), List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
