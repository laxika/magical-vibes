package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CloudsteelKirin;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.w.Wanderlust;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SigardasAid.class, GrizzlyBears.class, LeoninScimitar.class, Wanderlust.class, CloudsteelKirin.class})
class SigardasAidTest extends BaseCardTest {

    @Test
    @DisplayName("Aura and Equipment spells can be cast at instant speed")
    void grantsFlashToAurasAndEquipment() {
        harness.addToBattlefield(player1, new SigardasAid());
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Wanderlust()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, host.getId());
        assertThat(gd.stack).hasSize(1);

        gd.stack.clear();
        harness.castFromHand(player1, new LeoninScimitar(), "{1}");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Equipment entry trigger targets a creature you control and may attach the entering Equipment")
    void attachesEnteringEquipmentToChosenCreature() {
        harness.addToBattlefield(player1, new SigardasAid());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new LeoninScimitar(), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent equipment = findPermanent(player1, "Leonin Scimitar");
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Can attach creature Equipment with reconfigure through its entry trigger")
    void attachesCreatureEquipmentWithReconfigure() {
        harness.addToBattlefield(player1, new SigardasAid());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new CloudsteelKirin(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.runStateBasedActions();

        Permanent equipment = findPermanent(player1, "Cloudsteel Kirin");
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, equipment)).isFalse();
    }

    @Test
    @DisplayName("Declining the Equipment attachment leaves it unattached")
    void mayDeclineAttachment() {
        harness.addToBattlefield(player1, new SigardasAid());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new LeoninScimitar(), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Leonin Scimitar").getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("The Equipment entry trigger cannot target an opponent's creature")
    void targetsOnlyCreaturesYouControl() {
        harness.addToBattlefield(player1, new SigardasAid());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new LeoninScimitar(), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-Aura enchantments do not gain flash")
    void doesNotGrantFlashToOtherEnchantments() {
        harness.addToBattlefield(player1, new SigardasAid());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SigardasAid()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Sigarda's Aid does not grant flash to your Equipment")
    void doesNotGrantFlashToOpponent() {
        harness.addToBattlefield(player2, new SigardasAid());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Equipment entering does not trigger Sigarda's Aid")
    void doesNotTriggerForOpponentEquipment() {
        harness.addToBattlefield(player1, new SigardasAid());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent equipment = harness.enterBattlefieldAndReturn(player2, new LeoninScimitar());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equipment entry with no creatures leaves the Equipment unattached")
    void noCreaturesMeansNoAttachment() {
        harness.addToBattlefield(player1, new SigardasAid());
        harness.castFromHand(player1, new LeoninScimitar(), "{1}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Leonin Scimitar").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The attachment trigger resolves even if Sigarda's Aid leaves the battlefield")
    void triggerSurvivesSourceLeaving() {
        Permanent aid = harness.addToBattlefieldAndReturn(player1, new SigardasAid());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new LeoninScimitar(), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aid);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Leonin Scimitar").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipment entering without being cast still triggers attachment")
    void attachesEquipmentThatWasNotCast() {
        harness.addToBattlefield(player1, new SigardasAid());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent equipment = harness.enterBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }
}
