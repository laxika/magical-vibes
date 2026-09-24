package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MishrasBauble;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RatchetFieldMedic.class, RatchetRescueRacer.class, GrizzlyBears.class,
        MishrasBauble.class, SoulWarden.class})
class RatchetFieldMedicTest extends BaseCardTest {

    @Test
    void moreThanMeetsTheEyeCastsRatchetConvertedWithLivingMetal() {
        Permanent ratchet = castRatchetConverted();

        assertThat(ratchet.isTransformed()).isTrue();
        assertThat(ratchet.getCard()).isInstanceOf(RatchetRescueRacer.class);
        assertThat(gqs.isCreature(gd, ratchet)).isTrue();
        assertThat(gqs.isArtifact(ratchet)).isTrue();
    }

    @Test
    void lifeGainMayConvertRatchetAndReturnTappedArtifactWithinLifeCap() {
        harness.addToBattlefield(player1, new SoulWarden());
        MishrasBauble bauble = new MishrasBauble();
        harness.setGraveyard(player1, List.of(bauble));
        Permanent ratchet = harness.enterBattlefieldAndReturn(player1, new RatchetFieldMedic());

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bauble.getId()));
        harness.passBothPriorities();

        assertThat(ratchet.isTransformed()).isTrue();
        Permanent returned = findPermanent(player1, "Mishra's Bauble");
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    void nontokenArtifactLeavingBattlefieldConvertsRatchetOnce() {
        Permanent ratchet = castRatchetConverted();
        harness.addToBattlefield(player1, new MishrasBauble());

        harness.activateAbility(player1, 1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(ratchet.isTransformed()).isFalse();
        assertThat(ratchet.getCard()).isInstanceOf(RatchetFieldMedic.class);
    }

    @Test
    void tokenArtifactLeavingBattlefieldDoesNotConvertRatchet() {
        Permanent ratchet = castRatchetConverted();
        MishrasBauble tokenBauble = new MishrasBauble();
        tokenBauble.setToken(true);
        harness.addToBattlefield(player1, tokenBauble);

        harness.activateAbility(player1, 1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(ratchet.isTransformed()).isTrue();
        assertThat(ratchet.getCard()).isInstanceOf(RatchetRescueRacer.class);
    }

    private Permanent castRatchetConverted() {
        harness.setHand(player1, List.of(new RatchetFieldMedic()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Ratchet, Rescue Racer");
    }
}
