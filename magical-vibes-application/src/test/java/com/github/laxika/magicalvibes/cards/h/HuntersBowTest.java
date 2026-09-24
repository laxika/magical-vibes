package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuntersBow.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class HuntersBowTest extends BaseCardTest {

    @Test
    void entersAttachedAndDealsDamageEqualToEquippedCreaturesPower() {
        Permanent equippedCreature = addCreatureReady(player1, new HillGiant());
        Permanent victim = addCreatureReady(player2, new HillGiant());

        castBow(equippedCreature.getId(), victim.getId());
        resolveAllTriggers();

        Permanent bow = findPermanent(player1, "Hunter's Bow");
        assertThat(bow.getAttachedTo()).isEqualTo(equippedCreature.getId());
        assertThat(victim.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void canEnterWithoutChoosingTheOptionalDamageTarget() {
        Permanent equippedCreature = addCreatureReady(player1, new HillGiant());
        Permanent victim = addCreatureReady(player2, new HillGiant());

        castBow(equippedCreature.getId());
        resolveAllTriggers();

        Permanent bow = findPermanent(player1, "Hunter's Bow");
        assertThat(bow.getAttachedTo()).isEqualTo(equippedCreature.getId());
        assertThat(victim.getMarkedDamage()).isZero();
    }

    @Test
    void equippedCreatureGetsReachAndWard() {
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent bow = addPermanentReady(player1, new HuntersBow());
        bow.setAttachedTo(equippedCreature.getId());

        assertThat(gqs.hasKeyword(gd, equippedCreature, Keyword.REACH)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, equippedCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(equippedCreature.getMarkedDamage()).isZero();
    }

    @Test
    void equipAttachesToAnotherCreatureYouControl() {
        Permanent bow = addPermanentReady(player1, new HuntersBow());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(bow.getAttachedTo()).isEqualTo(creature.getId());
    }

    private void castBow(java.util.UUID... targets) {
        harness.setHand(player1, List.of(new HuntersBow()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gs.playCard(gd, player1, 0, 0, null, null, List.of(targets), List.of());
    }

    private Permanent addPermanentReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
