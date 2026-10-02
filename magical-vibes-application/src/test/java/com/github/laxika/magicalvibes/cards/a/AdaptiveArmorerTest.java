package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CloudsteelKirin;
import com.github.laxika.magicalvibes.cards.c.CitizensCrowbar;
import com.github.laxika.magicalvibes.cards.c.ConquerorsFlail;
import com.github.laxika.magicalvibes.cards.f.Fireshrieker;
import com.github.laxika.magicalvibes.cards.f.FishingPole;
import com.github.laxika.magicalvibes.cards.k.KrovodHaunch;
import com.github.laxika.magicalvibes.cards.l.LeechGauntlet;
import com.github.laxika.magicalvibes.cards.l.LionSash;
import com.github.laxika.magicalvibes.cards.m.MaceOfTheValiant;
import com.github.laxika.magicalvibes.cards.m.MaulOfTheSkyclaves;
import com.github.laxika.magicalvibes.cards.s.ShieldOfTheRealm;
import com.github.laxika.magicalvibes.cards.s.SigiledSwordOfValeron;
import com.github.laxika.magicalvibes.cards.t.ThranPowerSuit;
import com.github.laxika.magicalvibes.cards.t.ThunderLasso;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@CardUsed({AdaptiveArmorer.class, AmorphousAxe.class, CitizensCrowbar.class, CloudsteelKirin.class,
        ConquerorsFlail.class, Fireshrieker.class, FishingPole.class, KrovodHaunch.class,
        LeechGauntlet.class, LionSash.class, MaceOfTheValiant.class, MaulOfTheSkyclaves.class,
        ShieldOfTheRealm.class, SigiledSwordOfValeron.class, ThranPowerSuit.class,
        ThunderLasso.class})
class AdaptiveArmorerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB drafts an Equipment onto the battlefield and attaches it to a creature you control")
    void draftsAndAttachesEquipment() {
        Permanent target = addCreatureReady(player1, new AdaptiveArmorer());

        castAndResolveArmorer();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(3);
        Card selected = selectEquipment(choice);

        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        resolveAttachments(target);

        Permanent drafted = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(selected.getId()))
                .findFirst().orElseThrow();
        assertThat(drafted.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("The attachment target is restricted to creatures you control")
    void attachmentTargetMustBeControlledCreature() {
        Permanent ownCreature = addCreatureReady(player1, new AdaptiveArmorer());
        Permanent opponentCreature = addCreatureReady(player2, new AdaptiveArmorer());

        castAndResolveArmorer();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        Card selected = selectEquipment(choice);
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        Permanent armorer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AdaptiveArmorer
                        && !permanent.getId().equals(ownCreature.getId()))
                .findFirst().orElseThrow();
        assertThat(targetChoice.validPermanentIds())
                .contains(ownCreature.getId(), armorer.getId())
                .doesNotContain(opponentCreature.getId());

        resolveAttachments(ownCreature);
    }

    @Test
    @DisplayName("The draft and attachment still resolve after the Armorer leaves the battlefield")
    void triggerResolvesWithoutArmorer() {
        Permanent target = addCreatureReady(player1, new AdaptiveArmorer());
        harness.castFromHand(player1, new AdaptiveArmorer(), "{2}{W}{W}");
        harness.passBothPriorities();
        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(target.getId()))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        Card selected = selectEquipment(choice);
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        resolveAttachments(target);

        Permanent drafted = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(selected.getId()))
                .findFirst().orElseThrow();
        assertThat(drafted.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A drafted Equipment is on the battlefield before the attachment ability resolves")
    void attachmentUsesTheStack() {
        Permanent target = addCreatureReady(player1, new AdaptiveArmorer());
        castAndResolveArmorer();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        Card selected = selectEquipment(choice);
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        Permanent drafted = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(selected.getId()))
                .findFirst().orElseThrow();
        assertThat(drafted.getAttachedTo()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        resolveAttachments(target);
        assertThat(drafted.getAttachedTo()).isEqualTo(target.getId());
    }

    private Card selectEquipment(PendingInteraction.LibraryRevealChoice choice) {
        return choice.allCards().stream()
                .filter(card -> !(card instanceof CitizensCrowbar))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("A drafted Equipment creature cannot attach to itself")
    void equipmentCannotAttachToItself() {
        castAndResolveArmorer();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        Card selected = choice.allCards().stream()
                .filter(card -> card instanceof CloudsteelKirin
                        || card instanceof LeechGauntlet || card instanceof LionSash)
                .findFirst().orElse(null);
        assumeTrue(selected != null, "The random draft must offer an Equipment creature");
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        Permanent drafted = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(selected.getId()))
                .findFirst().orElseThrow();

        harness.handlePermanentChosen(player1, drafted.getId());
        resolveAllTriggers();

        assertThat(drafted.getAttachedTo()).isNull();
        assertThat(gameLogContains(selected.getName() + " is now attached to " + selected.getName()))
                .isFalse();
    }

    private void resolveAttachments(Permanent target) {
        while (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null
                || !gd.stack.isEmpty()) {
            if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
                harness.handlePermanentChosen(player1, target.getId());
            }
            resolveAllTriggers();
        }
    }

    private void castAndResolveArmorer() {
        harness.castFromHand(player1, new AdaptiveArmorer(), "{2}{W}{W}");
        resolveAllTriggers();
    }
}
