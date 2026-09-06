package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TyphoidRats;
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

@CardUsed({PersistentMarshstalker.class, TyphoidRats.class, GrizzlyBears.class})
class PersistentMarshstalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each other Rat you control")
    void scalesWithOtherRats() {
        Permanent marshstalker = harness.addToBattlefieldAndReturn(player1, new PersistentMarshstalker());
        int basePower = marshstalker.getCard().getPower();

        assertThat(gqs.getEffectivePower(gd, marshstalker)).isEqualTo(basePower);

        harness.addToBattlefield(player1, new TyphoidRats());
        assertThat(gqs.getEffectivePower(gd, marshstalker)).isEqualTo(basePower + 1);

        harness.addToBattlefield(player1, new TyphoidRats());
        assertThat(gqs.getEffectivePower(gd, marshstalker)).isEqualTo(basePower + 2);

        harness.addToBattlefield(player2, new TyphoidRats());
        assertThat(gqs.getEffectivePower(gd, marshstalker)).isEqualTo(basePower + 2);
    }

    @Test
    @DisplayName("Triggers when one or more Rats attack with threshold")
    void triggersForRatAttackWithThreshold() {
        Permanent rat = addCreatureReady(player1, new TyphoidRats());
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
    @DisplayName("Does not trigger for a non-Rat attack or without threshold")
    void requiresRatAndThreshold() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, graveyardWith(new PersistentMarshstalker(), 6));

        declareAttackers(List.of(0));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Persistent Marshstalker"));
    }

    @Test
    @DisplayName("Returns tapped and attacking after paying the threshold ability")
    void returnsTappedAndAttacking() {
        Permanent rat = addCreatureReady(player1, new TyphoidRats());
        PersistentMarshstalker marshstalker = new PersistentMarshstalker();
        harness.setGraveyard(player1, graveyardWith(marshstalker, 6));
        harness.addMana(player1, ManaColor.BLACK, 3);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(marshstalker.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttackedThisTurn()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(marshstalker.getId()));
        assertThat(rat.isAttackedThisTurn()).isTrue();
    }

    private List<com.github.laxika.magicalvibes.model.Card> graveyardWith(PersistentMarshstalker marshstalker,
                                                                            int additionalCards) {
        List<com.github.laxika.magicalvibes.model.Card> cards = new ArrayList<>();
        cards.add(marshstalker);
        for (int i = 0; i < additionalCards; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
