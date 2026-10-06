package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.k.KeenSense;
import com.github.laxika.magicalvibes.cards.s.SinewSliver;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.cards.u.UtopiaVow;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Retether.class, KeenSense.class, RealityAcid.class, SinewSliver.class,
        UrborgTombOfYawgmoth.class, UtopiaVow.class})
class RetetherTest extends BaseCardTest {

    @Test
    @DisplayName("Returns each Aura attached to a creature")
    void returnsEachAuraAttachedToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SinewSliver());
        Card aura = new KeenSense();
        harness.setGraveyard(player1, List.of(aura));
        castRetether();

        Permanent returnedAura = findPermanent(player1, "Keen Sense");
        assertThat(returnedAura.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertNotInGraveyard(player1, "Keen Sense");
    }

    @Test
    @DisplayName("Lets the controller choose among legal creature attachments")
    void letsControllerChooseAmongLegalCreatureAttachments() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new SinewSliver());
        Permanent chosenCreature = harness.addToBattlefieldAndReturn(player1, new SinewSliver());
        Card aura = new UtopiaVow();
        harness.setGraveyard(player1, List.of(aura));

        castRetether();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstCreature.getId(), chosenCreature.getId());

        harness.handlePermanentChosen(player1, chosenCreature.getId());

        Permanent returnedAura = findPermanent(player1, "Utopia Vow");
        assertThat(returnedAura.getAttachedTo()).isEqualTo(chosenCreature.getId());
    }

    @Test
    @DisplayName("Leaves an Aura in the graveyard when only a land can be enchanted")
    void leavesAuraWhenOnlyLandCanBeEnchanted() {
        harness.addToBattlefieldAndReturn(player1, new UrborgTombOfYawgmoth());
        Card aura = new RealityAcid();
        harness.setGraveyard(player1, List.of(aura));
        castRetether();

        harness.assertInGraveyard(player1, "Reality Acid");
        harness.assertNotOnBattlefield(player1, "Reality Acid");
    }

    @Test
    @DisplayName("Returns only Aura cards and leaves other graveyard cards behind")
    void returnsOnlyAuraCards() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SinewSliver());
        Card keenSense = new KeenSense();
        Card utopiaVow = new UtopiaVow();
        Card nonAura = new SinewSliver();
        harness.setGraveyard(player1, List.of(keenSense, utopiaVow, nonAura));
        castRetether();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(keenSense.getId())
                        && permanent.getAttachedTo().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(utopiaVow.getId())
                        && permanent.getAttachedTo().equals(creature.getId()));
        harness.assertInGraveyard(player1, "Sinew Sliver");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(nonAura.getId()));
    }

    @Test
    @DisplayName("Can attach a returned Aura to an opposing creature")
    void canAttachAuraToOpposingCreature() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new SinewSliver());
        Card aura = new KeenSense();
        harness.setGraveyard(player1, List.of(aura));
        castRetether();

        Permanent returnedAura = findPermanent(player1, "Keen Sense");
        assertThat(returnedAura.getAttachedTo()).isEqualTo(opposingCreature.getId());
    }

    @Test
    @DisplayName("Returns enchant-permanent Auras attached to creatures")
    void returnsEnchantPermanentAuraAttachedToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SinewSliver());
        harness.setGraveyard(player1, List.of(new RealityAcid()));

        castRetether();

        assertThat(findPermanent(player1, "Reality Acid").getAttachedTo()).isEqualTo(creature.getId());
        harness.assertNotInGraveyard(player1, "Reality Acid");
    }

    @Test
    @DisplayName("Leaves every Aura in the graveyard when there are no creatures")
    void leavesAurasWhenThereAreNoCreatures() {
        harness.setGraveyard(player1, List.of(new KeenSense(), new UtopiaVow()));

        castRetether();

        harness.assertInGraveyard(player1, "Keen Sense");
        harness.assertInGraveyard(player1, "Utopia Vow");
        harness.assertNotOnBattlefield(player1, "Keen Sense");
        harness.assertNotOnBattlefield(player1, "Utopia Vow");
        harness.assertInGraveyard(player1, "Retether");
    }

    @Test
    @DisplayName("Does not return Auras from the opponent's graveyard")
    void leavesOpponentsAurasInTheirGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SinewSliver());
        harness.setGraveyard(player1, List.of(new KeenSense()));
        harness.setGraveyard(player2, List.of(new UtopiaVow()));

        castRetether();

        assertThat(findPermanent(player1, "Keen Sense").getAttachedTo()).isEqualTo(creature.getId());
        harness.assertInGraveyard(player2, "Utopia Vow");
        harness.assertNotOnBattlefield(player1, "Utopia Vow");
        harness.assertNotOnBattlefield(player2, "Utopia Vow");
    }

    @Test
    @DisplayName("Resolves with an empty graveyard")
    void resolvesWithEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());

        castRetether();

        harness.assertInGraveyard(player1, "Retether");
        assertThat(gd.stack).isEmpty();
    }

    private void castRetether() {
        harness.castFromHand(player1, new Retether(), "{3}{W}");
        harness.passBothPriorities();
    }
}
