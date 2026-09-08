package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShacklesOfTreacheryTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps, steals, and grants haste to the target creature")
    void untapsStealsAndGrantsHaste() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        castShackles(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("The damage trigger targets only Equipment attached to the stolen creature")
    void damageTriggerTargetsAttachedEquipment() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent attachedEquipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        attachedEquipment.setAttachedTo(target.getId());
        Permanent unattachedEquipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        castShackles(target);
        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SelfTriggeredAbilityTarget.class);
        assertThat(((PendingInteraction.PermanentChoice) gd.interaction.activeInteraction()).validIds())
                .contains(attachedEquipment.getId())
                .doesNotContain(unattachedEquipment.getId());

        harness.handlePermanentChosen(player1, attachedEquipment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Leonin Scimitar");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(unattachedEquipment.getId()));
    }

    @Test
    @DisplayName("The damage trigger is skipped when the stolen creature has no attached Equipment")
    void damageTriggerIsSkippedWithoutAttachedEquipment() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castShackles(target);
        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.SelfTriggeredAbilityTarget.class)).isFalse();
    }

    private void castShackles(Permanent target) {
        harness.setHand(player1, List.of(new ShacklesOfTreachery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
