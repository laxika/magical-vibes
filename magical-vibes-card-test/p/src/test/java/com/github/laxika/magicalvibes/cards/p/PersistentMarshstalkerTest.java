package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.r.RavineRaider;
import com.github.laxika.magicalvibes.cards.r.RalCracklingWit;
import com.github.laxika.magicalvibes.cards.s.Savor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PersistentMarshstalker.class, RavineRaider.class, Savor.class, RalCracklingWit.class})
class PersistentMarshstalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each other Rat you control")
    void scalesWithOtherRats() {
        Permanent marshstalker = harness.addToBattlefieldAndReturn(player1, new PersistentMarshstalker());
        int basePower = marshstalker.getCard().getPower();

        assertThat(gqs.getEffectivePower(gd, marshstalker)).isEqualTo(basePower);

        harness.addToBattlefield(player1, new PersistentMarshstalker());
        assertThat(gqs.getEffectivePower(gd, marshstalker)).isEqualTo(basePower + 1);

        harness.addToBattlefield(player1, new PersistentMarshstalker());
        assertThat(gqs.getEffectivePower(gd, marshstalker)).isEqualTo(basePower + 2);

        harness.addToBattlefield(player2, new PersistentMarshstalker());
        assertThat(gqs.getEffectivePower(gd, marshstalker)).isEqualTo(basePower + 2);
    }

    @Test
    @DisplayName("Triggers when one or more Rats attack with threshold")
    void triggersForRatAttackWithThreshold() {
        Permanent rat = addCreatureReady(player1, new PersistentMarshstalker());
        PersistentMarshstalker marshstalker = new PersistentMarshstalker();
        harness.setGraveyard(player1, graveyardWith(marshstalker, 6));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{2}{B}");
        assertThat(rat.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger for a non-Rat attack")
    void requiresRatAttack() {
        addCreatureReady(player1, new RavineRaider());
        harness.setGraveyard(player1, graveyardWith(new PersistentMarshstalker(), 6));

        declareAttackers(List.of(0));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Persistent Marshstalker"));
    }

    @Test
    @DisplayName("Returns tapped and attacking after paying the threshold ability")
    void returnsTappedAndAttacking() {
        Permanent rat = addCreatureReady(player1, new PersistentMarshstalker());
        PersistentMarshstalker marshstalker = new PersistentMarshstalker();
        harness.setGraveyard(player1, graveyardWith(marshstalker, 6));
        harness.addMana(player1, ManaColor.BLACK, 3);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanents(player1, "Persistent Marshstalker").stream()
                .filter(permanent -> permanent != rat).findFirst().orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttacking()).isTrue();
        assertThat(returned.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(marshstalker.getId()));
        assertThat(rat.isAttackedThisTurn()).isTrue();
    }

    @Test
    void canChooseAPlaneswalkerForTheReturnedCreatureToAttack() {
        addCreatureReady(player1, new PersistentMarshstalker());
        Permanent ral = harness.enterBattlefieldAndReturn(player2, new RalCracklingWit());
        PersistentMarshstalker marshstalker = new PersistentMarshstalker();
        harness.setGraveyard(player1, graveyardWith(marshstalker, 6));
        harness.addMana(player1, ManaColor.BLACK, 3);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, ral.getId());
        Permanent returned = findPermanents(player1, "Persistent Marshstalker").stream()
                .filter(permanent -> permanent.getCard().getId().equals(marshstalker.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttacking()).isTrue();
        assertThat(returned.getAttackTarget()).isEqualTo(ral.getId());
    }

    @Test
    void doesNotTriggerBelowThreshold() {
        addCreatureReady(player1, new PersistentMarshstalker());
        harness.setGraveyard(player1, graveyardWith(new PersistentMarshstalker(), 5));

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void triggersOnlyOnceForMultipleAttackingRatsAndCanBeDeclined() {
        addCreatureReady(player1, new PersistentMarshstalker());
        addCreatureReady(player1, new PersistentMarshstalker());
        harness.setGraveyard(player1, graveyardWith(new PersistentMarshstalker(), 6));
        harness.addMana(player1, ManaColor.BLACK, 3);

        declareAttackers(List.of(0, 1));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Persistent Marshstalker")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Persistent Marshstalker");
    }

    @Test
    void rechecksThresholdAtResolution() {
        addCreatureReady(player1, new PersistentMarshstalker());
        PersistentMarshstalker marshstalker = new PersistentMarshstalker();
        harness.setGraveyard(player1, graveyardWith(marshstalker, 6));
        harness.addMana(player1, ManaColor.BLACK, 3);

        declareAttackers(List.of(0));
        harness.setGraveyard(player1, graveyardWith(marshstalker, 5));
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(countPermanents(player1, "Persistent Marshstalker")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Persistent Marshstalker");
    }

    @Test
    void stillOffersPaymentAfterTheAttackingRatDies() {
        Permanent rat = addCreatureReady(player1, new PersistentMarshstalker());
        harness.setGraveyard(player1, graveyardWith(new PersistentMarshstalker(), 6));
        harness.setHand(player2, List.of(new Savor()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        declareAttackers(List.of(0));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, rat.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rat);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }

    private List<com.github.laxika.magicalvibes.model.Card> graveyardWith(PersistentMarshstalker marshstalker,
                                                                            int additionalCards) {
        List<com.github.laxika.magicalvibes.model.Card> cards = new ArrayList<>();
        cards.add(marshstalker);
        for (int i = 0; i < additionalCards; i++) {
            cards.add(new RavineRaider());
        }
        return cards;
    }
}
