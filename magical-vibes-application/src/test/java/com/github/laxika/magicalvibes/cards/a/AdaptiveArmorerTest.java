package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CloudsteelKirin;
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
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdaptiveArmorer.class, AmorphousAxe.class, CloudsteelKirin.class,
        ConquerorsFlail.class, Fireshrieker.class, FishingPole.class, KrovodHaunch.class,
        LeechGauntlet.class, LionSash.class, MaceOfTheValiant.class, MaulOfTheSkyclaves.class,
        ShieldOfTheRealm.class, SigiledSwordOfValeron.class, ThranPowerSuit.class,
        ThunderLasso.class, GrizzlyBears.class})
class AdaptiveArmorerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB drafts an Equipment onto the battlefield and attaches it to a creature you control")
    void draftsAndAttachesEquipment() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AdaptiveArmorer()));
        addManaForArmorer();

        castAndResolveArmorer();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(3);
        Card selected = choice.allCards().getFirst();

        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent drafted = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(selected.getId()))
                .findFirst().orElseThrow();
        assertThat(drafted.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("The attachment target is restricted to creatures you control")
    void attachmentTargetMustBeControlledCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AdaptiveArmorer()));
        addManaForArmorer();

        castAndResolveArmorer();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        Card selected = choice.allCards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        Permanent armorer = findPermanent(player1, "Adaptive Armorer");
        assertThat(targetChoice.validPermanentIds())
                .contains(ownCreature.getId(), armorer.getId())
                .doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();
    }

    private void addManaForArmorer() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void castAndResolveArmorer() {
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
