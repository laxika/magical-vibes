package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlitchInterpreter.class, Forest.class, GrizzlyBears.class, Memnite.class})
class GlitchInterpreterTest extends BaseCardTest {

    @Test
    void returnsToHandAndManifestsDreadWhenNoFaceDownPermanentIsControlled() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new GlitchInterpreter()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof GlitchInterpreter);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    void doesNotReturnOrManifestWhenAFaceDownPermanentIsControlled() {
        Permanent faceDown = new Permanent(new GrizzlyBears());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        gd.playerBattlefields.get(player1.getId()).add(faceDown);

        GlitchInterpreter glitchInterpreter = new GlitchInterpreter();
        harness.setHand(player1, List.of(glitchInterpreter));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == glitchInterpreter);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(glitchInterpreter);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void drawsOnceWhenMultipleColorlessCreaturesDealCombatDamage() {
        addFaceDownPermanent();
        harness.addToBattlefield(player1, new GlitchInterpreter());
        addAttacker(new Memnite());
        addAttacker(new Memnite());
        addAttacker(new GrizzlyBears());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        runCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void doesNotDrawForColoredCreatureCombatDamage() {
        addFaceDownPermanent();
        harness.addToBattlefield(player1, new GlitchInterpreter());
        addAttacker(new GrizzlyBears());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        runCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).isEmpty();
    }

    private void addFaceDownPermanent() {
        Permanent faceDown = new Permanent(new GrizzlyBears());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        gd.playerBattlefields.get(player1.getId()).add(faceDown);
    }

    private void addAttacker(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
    }

    private void runCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
