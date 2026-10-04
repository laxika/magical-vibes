package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FangRokusCompanion.class, GrizzlyBears.class, IsamaruHoundOfKonda.class})
class FangRokusCompanionTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger targets another legendary creature you control")
    void attackTriggerTargetsAnotherLegendaryCreatureYouControl() {
        Permanent fang = addCreatureReady(player1, new FangRokusCompanion());
        Permanent legendary = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(legendary.getId());
        assertThat(choice.validIds()).doesNotContain(fang.getId(), opponentCreature.getId());

        harness.handlePermanentChosen(player1, legendary.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, legendary)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, legendary)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns from death as a Spirit, but not after dying as a Spirit")
    void returnsOnceAsSpirit() {
        Permanent fang = addCreatureReady(player1, new FangRokusCompanion());

        kill(fang);

        Permanent returned = findPermanent(player1, "Fang, Roku's Companion");
        assertThat(returned.getGrantedSubtypes()).contains(CardSubtype.SPIRIT);
        harness.assertNotInGraveyard(player1, "Fang, Roku's Companion");

        kill(returned);

        harness.assertNotOnBattlefield(player1, "Fang, Roku's Companion");
        harness.assertInGraveyard(player1, "Fang, Roku's Companion");
    }

    @Test
    @DisplayName("Attack bonus uses Fang's power when the trigger resolves")
    void attackBonusUsesPowerAtResolution() {
        Permanent fang = addCreatureReady(player1, new FangRokusCompanion());
        Permanent legendary = addCreatureReady(player1, new IsamaruHoundOfKonda());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, legendary.getId());
        fang.setPowerModifier(3);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, legendary)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, legendary)).isEqualTo(2);
        fang.setPowerModifier(0);
        assertThat(gqs.getEffectivePower(gd, legendary)).isEqualTo(9);
    }

    @Test
    @DisplayName("Attack bonus uses last known power after Fang dies and returns")
    void attackBonusUsesLastKnownPowerAfterDeath() {
        Permanent fang = addCreatureReady(player1, new FangRokusCompanion());
        Permanent legendary = addCreatureReady(player1, new IsamaruHoundOfKonda());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, legendary.getId());
        fang.setPowerModifier(3);
        fang.setMarkedDamage(gqs.getEffectiveToughness(gd, fang));
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, legendary)).isEqualTo(9);
        assertThat(findPermanent(player1, "Fang, Roku's Companion").getId()).isNotEqualTo(fang.getId());
    }

    @Test
    @DisplayName("An opposing legendary creature and a friendly nonlegendary creature are not targets")
    void attackHasNoLegalTargetWithOnlyIneligibleCreatures() {
        addCreatureReady(player1, new FangRokusCompanion());
        Permanent friendly = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new IsamaruHoundOfKonda());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, friendly)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(2);
    }

    @Test
    @DisplayName("A stolen Fang returns under its last controller's control")
    void stolenFangReturnsToControllerRatherThanOwner() {
        Permanent fang = addCreatureReady(player2, new FangRokusCompanion());
        gd.stolenCreatures.put(fang.getId(), player1.getId());

        kill(fang);

        harness.assertNotOnBattlefield(player1, "Fang, Roku's Companion");
        Permanent returned = findPermanent(player2, "Fang, Roku's Companion");
        assertThat(returned.getGrantedSubtypes()).contains(CardSubtype.SPIRIT);
        harness.assertNotInGraveyard(player1, "Fang, Roku's Companion");

        kill(returned);

        harness.assertNotOnBattlefield(player2, "Fang, Roku's Companion");
        harness.assertInGraveyard(player1, "Fang, Roku's Companion");
    }

    @Test
    @DisplayName("Fang already made a Spirit does not return from death")
    void alreadySpiritDoesNotReturn() {
        Permanent fang = addCreatureReady(player1, new FangRokusCompanion());
        fang.getGrantedSubtypes().add(CardSubtype.SPIRIT);

        kill(fang);

        harness.assertNotOnBattlefield(player1, "Fang, Roku's Companion");
        harness.assertInGraveyard(player1, "Fang, Roku's Companion");
    }

    @Test
    @DisplayName("Fang cannot return if its card leaves the graveyard before resolution")
    void cannotReturnAfterLeavingGraveyard() {
        Permanent fang = addCreatureReady(player1, new FangRokusCompanion());
        fang.setMarkedDamage(gqs.getEffectiveToughness(gd, fang));
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Fang, Roku's Companion");
        gd.playerGraveyards.get(player1.getId()).remove(fang.getCard());
        harness.setExile(player1, List.of(fang.getCard()));

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Fang, Roku's Companion");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(fang.getCard());
    }

    @Test
    @DisplayName("The attack bonus expires at end of turn")
    void attackBonusExpiresAtEndOfTurn() {
        addCreatureReady(player1, new FangRokusCompanion());
        Permanent legendary = addCreatureReady(player1, new IsamaruHoundOfKonda());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, legendary.getId());
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, legendary)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, legendary)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, legendary)).isEqualTo(2);
    }
    private void kill(Permanent creature) {
        creature.setMarkedDamage(creature.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities();
    }
}
