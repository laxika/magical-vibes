package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShacklesOfTreachery.class, GrizzlyBears.class, LeoninScimitar.class})
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

    @Test
    @DisplayName("Equipment detached before the damage trigger resolves is not destroyed")
    void detachedEquipmentIsNotDestroyed() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.setAttachedTo(target.getId());

        castShackles(target);
        declareAttackers(List.of(0));
        resolveCombat();
        harness.handlePermanentChosen(player1, equipment.getId());

        equipment.setAttachedTo(null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        harness.assertNotInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Control, haste, and the granted damage ability expire at end of turn")
    void temporaryEffectsExpire() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.setAttachedTo(target.getId());

        castShackles(target);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(target)));
        resolveCombat(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Can untap and grant the damage ability to a creature already controlled by the caster")
    void canTargetOwnCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(target.getId());

        castShackles(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        declareAttackers(List.of(0));
        resolveCombat();
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }

    private void castShackles(Permanent target) {
        harness.setHand(player1, List.of(new ShacklesOfTreachery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }
}
