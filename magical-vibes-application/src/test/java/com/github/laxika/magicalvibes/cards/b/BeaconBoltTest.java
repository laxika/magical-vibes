package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MagmaJet;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeaconBolt.class, CrawWurm.class, GrizzlyBears.class, MagmaJet.class,
        Mountain.class, Plains.class, Shock.class})
class BeaconBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage for instant and sorcery cards in the controller's graveyard and exile")
    void dealsDamageForInstantAndSorceryCardsInGraveyardAndExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        harness.setGraveyard(player1, List.of(new MagmaJet(), new Shock(), new Mountain()));
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.setExile(player1, List.of(new MagmaJet(), new Mountain()));
        harness.setHand(player1, List.of(new BeaconBolt()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Jump-start discards a card, deals damage, and exiles Beacon Bolt")
    void jumpStartDiscardsDealsDamageAndExiles() {
        BeaconBolt spell = new BeaconBolt();
        Plains discarded = new Plains();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell));
        harness.setExile(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(discarded));
        addMana();

        harness.castJumpStart(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new BeaconBolt()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The resolving Beacon Bolt does not count itself")
    void dealsZeroDamageWithNoOtherSpellCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BeaconBolt()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Beacon Bolt");
    }

    @Test
    @DisplayName("Jump-start counts a discarded sorcery but not the spell on the stack")
    void countsDiscardedSorceryForJumpStart() {
        BeaconBolt spell = new BeaconBolt();
        BeaconBolt discarded = new BeaconBolt();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        addMana();

        harness.castJumpStart(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getId()).containsExactly(discarded.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId()).containsExactly(spell.getId());
    }

    @Test
    @DisplayName("Damage uses the graveyard and exile counts at resolution")
    void countsCardsAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        harness.setHand(player1, List.of(new BeaconBolt()));
        addMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(new BeaconBolt()));
        harness.setExile(player1, List.of(new BeaconBolt()));
        harness.setExile(player2, List.of(new BeaconBolt()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Face-down exiled cards have no types and do not increase damage")
    void excludesFaceDownExiledCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.addToExile(player1.getId(), new BeaconBolt(), null, true);
        harness.setHand(player1, List.of(new BeaconBolt()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Jump-start requires a card to discard")
    void cannotJumpStartWithoutDiscard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new BeaconBolt()));
        harness.setHand(player1, List.of());
        addMana();

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Beacon Bolt");
    }

    @Test
    @DisplayName("Jump-start exiles the spell even when its only target is gone")
    void jumpStartExilesWhenTargetIsGone() {
        BeaconBolt spell = new BeaconBolt();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(new Plains()));
        addMana();

        harness.castJumpStart(player1, 0, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId()).containsExactly(spell.getId());
        harness.assertNotInGraveyard(player1, "Beacon Bolt");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
